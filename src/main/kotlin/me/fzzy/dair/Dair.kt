package me.fzzy.dair

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import me.fzzy.dair.commands.Score
import me.fzzy.dair.util.Players
import sx.blah.discord.Discord4J
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuilder
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

val gson = Gson()

val playersFile = File("data${File.separator}players.json")

fun main(args: Array<String>) {
    val discordToken = args[0]

    Bot.client = ClientBuilder().withToken(discordToken).build()
    if (playersFile.exists()) Bot.players =
        gson.fromJson(JsonReader(InputStreamReader(playersFile.inputStream())), Players::class.java)

    CommandHandler.registerCommand("score", Score)

    Bot.client.dispatcher.registerListener(CommandHandler)
    Discord4J.LOGGER.info("Logging in.")
    Bot.client.login()
}

object Bot {
    var players = Players()

    const val LEADERBOARD_CHANNEL_ID = 562445542441877529

    lateinit var client: IDiscordClient

    const val BOT_PREFIX = "."

    fun save() {
        File("data").mkdirs()
        val bufferWriter = BufferedWriter(FileWriter(playersFile.absoluteFile, false))
        val save = gson.toJson(Bot.players)
        bufferWriter.write(save)
        bufferWriter.close()
    }

    fun getLeaderboard(): List<Player> {
        return Bot.players.all.sortedByDescending { it.elo }
    }

    fun updateLeaderboard() {
        val channel = Bot.client.getChannelByID(Bot.LEADERBOARD_CHANNEL_ID)
        val msgBuilder = RequestBuilder(client).shouldBufferRequests(true).doAction { true }
        for (msg in channel.fullMessageHistory) {
            msgBuilder.andThen {
                msg.delete()
                true
            }
        }
        var builder = EmbedBuilder()
        val leaderboard = getLeaderboard()
        for (i in leaderboard.size - 1 downTo 0) {
            val player = leaderboard[i]

            val title = "#${i + 1} - ${player.name}"
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
        }


        msgBuilder.execute()
    }
}