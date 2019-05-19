package me.fzzy.dair.commands

import me.fzzy.dair.*
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
                "delete" -> {
                    val rankToDelete = args[1].toInt()
                    val player = Bot.getLeaderboard()[rankToDelete - 1]
                    Bot.matches.deletedIds.add(player.p.id)
                }
                "update" -> {
                    Bot.updateLeaderboard(event.channel)
                }
                else -> {
                    val api = SmashGGApi(args[0].toInt())

                    for (set in api.sets.values) {
                        if (set.entrant1Score == -1 || set.entrant2Score == -1) continue

                        val player1 = Player(set.entrant1Name, set.entrant1Id)
                        val player2 = Player(set.entrant2Name, set.entrant2Id)

                        if (player1.name.isBlank() || player2.name.isBlank()) continue

                        val match = Matches.Match(player1, player2, set.entrant1Score > set.entrant2Score)
                        Bot.matches.matches.add(match)
                        match.doMatch()
                    }
                }
            }
        }

        Bot.save()
        return CommandResult.success()
    }

}