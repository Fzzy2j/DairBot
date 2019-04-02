package me.fzzy.dair.commands

import me.fzzy.dair.Bot
import me.fzzy.dair.Command
import me.fzzy.dair.CommandResult
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.RequestBuilder

object LeaderboardCommand : Command {

    override val description = "shows the vote leaderboard"
    override val usageText: String = "leaderboard"
    override val allowDM: Boolean = false

    override fun runCommand(event: MessageReceivedEvent, args: List<String>): CommandResult {
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
                builder.withColor(0, 200, 255)
                val msg = builder.build()
                msgBuilder.andThen {
                    event.channel.sendMessage(msg)
                    true
                }
                builder = EmbedBuilder()
            }
        }

        msgBuilder.execute()

        return CommandResult.success()
    }

}