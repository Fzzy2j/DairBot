package me.fzzy.dair

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import me.fzzy.dair.commands.LeaderboardCommand
import me.fzzy.dair.commands.Score
import me.fzzy.dair.util.Players
import sx.blah.discord.Discord4J
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

val gson = Gson()

val playersFile = File("data${File.separator}players.json")

fun main(args: Array<String>) {
    val discordToken = args[0]

    Bot.client = ClientBuilder().withToken(discordToken).build()
    if (playersFile.exists()) Bot.players = gson.fromJson(JsonReader(InputStreamReader(playersFile.inputStream())), Players::class.java)
    for (player in Bot.players.all) {
        Bot.leaderboard.setValue(player.id, player.elo)
    }

    CommandHandler.registerCommand("leaderboard", LeaderboardCommand)
    CommandHandler.registerCommand("score", Score)

    Bot.client.dispatcher.registerListener(CommandHandler)
    Discord4J.LOGGER.info("Logging in.")
    Bot.client.login()
}

object Bot {
    var players = Players()

    val leaderboard = Leaderboard()

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
}