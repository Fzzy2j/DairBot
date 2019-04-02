package me.fzzy.dair

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

interface Command {

    val description: String
    val usageText: String
    val allowDM: Boolean

    fun runCommand(event: MessageReceivedEvent, args: List<String>): CommandResult

}