package me.fzzy.dair

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import me.fzzy.dair.commands.Score
import me.fzzy.dair.util.Players
import sx.blah.discord.Discord4J
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuilder
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

val gson = Gson()

val matchesFile = File("data${File.separator}matches.json")

fun main(args: Array<String>) {
    val discordToken = args[0]

    Bot.client = ClientBuilder().withToken(discordToken).build()

    CommandHandler.registerCommand("score", Score)

    Bot.load()

    Bot.client.dispatcher.registerListener(CommandHandler)
    Discord4J.LOGGER.info("Logging in.")
    Bot.client.login()
}

object Bot {
    var players = Players()
    var matches = Matches()

    lateinit var client: IDiscordClient

    const val BOT_PREFIX = "."

    fun save() {
        File("data").mkdirs()
        val bufferWriter = BufferedWriter(FileWriter(matchesFile.absoluteFile, false))
        val save = gson.toJson(matches)
        bufferWriter.write(save)
        bufferWriter.close()
    }

    fun load() {
        if (matchesFile.exists()) {
            matches = gson.fromJson(JsonReader(InputStreamReader(matchesFile.inputStream())), Matches::class.java)
            for (match in matches.matches) {
                match.doMatch()
            }
        }
    }

    fun getLeaderboard(): List<Players.LivePlayer> {
        return players.all.sortedByDescending { it.elo }
    }

    fun updateLeaderboard(channel: IChannel) {
        val msgBuilder = RequestBuilder(client).shouldBufferRequests(true).doAction { true }
        for (msg in channel.fullMessageHistory) {
            msgBuilder.andThen {
                msg.delete()
                true
            }
        }
        var builder = EmbedBuilder()
        val leaderboard = getLeaderboard()
        var rank = leaderboard.size - 1
        for (i in leaderboard.size - 1 downTo 0) {
            val player = leaderboard[i]

            if (!matches.deletedIds.contains(player.p.id)) {
                val title = "#${rank + 1} - ${player.p.name}"
                val description = "${Math.round(player.elo)} points"
                builder.appendField(title, description, false)
                if (i % 25 == 0) {
                    builder.withColor(0, 103, 231)
                    val msg = builder.build()
                    msgBuilder.andThen {
                        channel.sendMessage(msg)
                        true
                    }
                    builder = EmbedBuilder()
                }
                rank--
            }
        }


        msgBuilder.execute()
    }
}