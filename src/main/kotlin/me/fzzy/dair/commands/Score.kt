package me.fzzy.dair.commands

import me.fzzy.dair.*
import me.fzzy.dair.util.MatchSet
import me.fzzy.dair.util.SmashGGApi
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Score : Command {
    override val description: String = "adds scores from a smash.gg tournament into the leaderboard"
    override val usageText: String = "score [tourneyId]"
    override val allowDM: Boolean = true

    override fun runCommand(event: MessageReceivedEvent, args: List<String>): CommandResult {

        if (event.author.longID != Bot.client.applicationOwner.longID) return CommandResult.success()
        if (args.isNotEmpty()) {
            when (args[0]) {
                "update" -> {
                    Bot.updateLeaderboard(event.channel, Bot.glicko2Board)
                }
                "recalc" -> {
                    Bot.recalculate()
                }
                else -> {
                    val id = args[0].split("/")
                    val api = SmashGGApi(id[id.count() - 1].toInt())

                    val matches = arrayListOf<Match>()
                    for (set in api.sets.values) {
                        if (set.entrant1Score == -1 || set.entrant2Score == -1) continue

                        if (set.entrant1Name.isBlank() || set.entrant2Name.isBlank()) continue

                        for (i in 0 until set.entrant1Score) {
                            val match = Match(set.entrant1Name, set.entrant2Name)
                            matches.add(match)
                        }
                        for (i in 0 until set.entrant2Score) {
                            val match = Match(set.entrant2Name, set.entrant1Name)
                            matches.add(match)
                        }
                    }
                    val set = MatchSet(args[0], matches)
                    Bot.addSet(set)
                }
            }
        }

        Bot.save()
        return CommandResult.success()
    }

}