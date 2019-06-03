package me.fzzy.dair

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import me.fzzy.dair.boards.EloBoard
import me.fzzy.dair.boards.Glicko2Board
import me.fzzy.dair.boards.glicko2.RatingCalculator
import me.fzzy.dair.commands.Score
import me.fzzy.dair.util.MatchSet
import org.json.JSONArray
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
import java.text.DecimalFormat

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
    private var sets = arrayListOf<MatchSet>()
    val eloBoard = EloBoard()
    val glicko2Board = Glicko2Board()

    lateinit var client: IDiscordClient

    const val BOT_PREFIX = "."

    fun addSet(set: MatchSet) {
        this.sets.add(set)
        eloBoard.doMatches(set)
        glicko2Board.doMatches(set)
    }

    fun recalculate() {
        eloBoard.clear()
        glicko2Board.clear()
        for (match in sets) {
            eloBoard.doMatches(match)
            glicko2Board.doMatches(match)
        }
    }

    fun save() {
        File("data").mkdirs()
        val bufferWriter = BufferedWriter(FileWriter(matchesFile.absoluteFile, false))
        val save = JSONArray(gson.toJson(sets))
        bufferWriter.write(save.toString(2))
        bufferWriter.close()
    }

    fun load() {
        if (matchesFile.exists()) {
            val type = object : TypeToken<ArrayList<MatchSet>>() {}.type
            val sets: ArrayList<MatchSet> =
                gson.fromJson(JsonReader(InputStreamReader(matchesFile.inputStream())), type)
            for (set in sets) {
                addSet(set)
            }
        }
    }

    fun updateLeaderboard(channel: IChannel, board: Board) {
        val msgBuilder = RequestBuilder(client).shouldBufferRequests(true).doAction { true }
        for (msg in channel.fullMessageHistory) {
            msgBuilder.andThen {
                msg.delete()
                true
            }
        }
        var builder = EmbedBuilder()
        for (i in board.leaderboard.size() downTo 1) {
            val player = board.leaderboard.getAtRank(i)!!
            val rank = board.leaderboard.getRank(player)!!
            val score = board.leaderboard.getScore(player)!!

            val title = "#$rank - $player"
            var description = "$score points"
            if (board is Glicko2Board) {
                val minimum = board.glickoPlayers[player]!!.glicko2Rating - board.glickoPlayers[player]!!.glicko2RatingDeviation * 2
                val eloStyle = minimum * RatingCalculator.MULTIPLIER + RatingCalculator.DEFAULT_RATING
                description = "${Math.round(eloStyle)} - ${Math.round(board.glickoPlayers[player]!!.glicko2RatingDeviation * 100)}"
            }
            builder.appendField(title, description, false)
            if (i % 25 == 1) {
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