package me.fzzy.dair.commands

import me.fzzy.dair.Bot
import me.fzzy.dair.Command
import me.fzzy.dair.CommandResult
import me.fzzy.dair.util.Elo
import me.fzzy.dair.util.SmashGGApi
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.RequestBuilder

object Score : Command {
    override val description: String = "adds scores from a smash.gg tournament into the leaderboard"
    override val usageText: String = "score [tourneyId]"
    override val allowDM: Boolean = true

    override fun runCommand(event: MessageReceivedEvent, args: List<String>): CommandResult {

        if (event.author.longID == Bot.client.applicationOwner.longID) {
            val api = SmashGGApi(args[0].toInt())

            for (set in api.sets.values) {
                val p1 = Bot.players.getOrDefault(set.entrant1Id, set.entrant1Name)
                val p2 = Bot.players.getOrDefault(set.entrant2Id, set.entrant2Name)
                if (p1.name.isBlank() || p2.name.isBlank()) continue

                if (set.entrant1Score != -1) p1.wins += set.entrant1Score
                if (set.entrant2Score != -1) p2.wins += set.entrant2Score

                if (set.entrant1Score > set.entrant2Score)
                    Elo.doMatch(p1, p2, true)
                else
                    Elo.doMatch(p1, p2, false)

                Bot.players.set(p1)
                Bot.players.set(p2)
            }

            val channel = Bot.client.getChannelByID(Bot.LEADERBOARD_CHANNEL_ID)
            for (msg in channel.fullMessageHistory) {
                RequestBuffer.request { msg.delete() }
            }
            var builder = EmbedBuilder()
            val msgBuilder = RequestBuilder(Bot.client).shouldBufferRequests(true).doAction { true }
            for (i in Bot.leaderboard.valueMap.size downTo 1) {
                val id = Bot.leaderboard.getAtRank(i)
                if (id != null) {
                    val value = Bot.leaderboard.getOrDefault(id, 0f)

                    val title = "#$i - ${Bot.players.get(id)!!.name}"
                    val description = "${Math.round(value)} points"
                    builder.appendField(title, description, false)
                }
                if ((i - 1) % 25 == 0) {
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

            Bot.save()
        }
        return CommandResult.success()
    }

}