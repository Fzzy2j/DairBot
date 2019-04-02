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

        if (event.author.longID != Bot.client.applicationOwner.longID) return CommandResult.success()
        if (args.isNotEmpty()) {
            when (args[0]) {
                "delete" -> {
                    val rankToDelete = args[1].toInt()
                    Bot.players.remove(Bot.getLeaderboard()[rankToDelete - 1])
                }
                else -> {
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
                }
            }
        }

        Bot.updateLeaderboard()
        Bot.save()
        return CommandResult.success()
    }

}