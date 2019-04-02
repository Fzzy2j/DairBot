package me.fzzy.dair

import sx.blah.discord.Discord4J
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RequestBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object CommandHandler {

    private var commandMap: HashMap<String, Command> = hashMapOf()

    fun registerCommand(string: String, command: Command) {
        commandMap[string.toLowerCase()] = command
    }

    fun getCommand(string: String): Command? {
        return commandMap[string.toLowerCase()]
    }

    fun getAllCommands(): HashMap<String, Command> {
        return commandMap
    }

    fun unregisterCommand(string: String): Boolean {
        if (commandMap.containsKey(string)) {
            commandMap.remove(string)
            return true
        }
        return false
    }

    @EventSubscriber
    fun onMessageReceived(event: MessageReceivedEvent) {
        val args = event.message.content.split(" ")
        if (args.isEmpty())
            return
        if (!args[0].startsWith(Bot.BOT_PREFIX))
            return
        val commandString = args[0].substring(1)
        var argsList: List<String> = args.toMutableList()
        argsList = argsList.drop(1)

        if (commandMap.containsKey(commandString.toLowerCase())) {

            val command = commandMap[commandString.toLowerCase()]!!

            if (event.channel.isPrivate) {
                if (!command.allowDM) {
                    RequestBuffer.request { event.channel.sendMessage("This command is not allowed in DMs!") }
                    return
                }
            }

            val date = SimpleDateFormat("hh:mm:ss aa").format(Date(System.currentTimeMillis()))
            Discord4J.LOGGER.info("$date - ${event.author.name}#${event.author.discriminator} running command: ${event.message.content}")
            try {
                command.runCommand(event, argsList)
            } catch (e: Exception) {
                e.printStackTrace()
                Discord4J.LOGGER.info("Command failed with message: ${e.message}")
                RequestBuffer.request { event.channel.sendMessage(e.message) }
                tryDelete(event.message)
            }
        }
    }

    fun tryDelete(msg: IMessage) {
        RequestBuffer.request {
            try {
                msg.delete()
            } catch (e: MissingPermissionsException) {
            } catch (e: DiscordException) {
            }
        }
    }
}

class CommandResult private constructor(private val success: Boolean, private val message: String) {

    companion object {
        fun success(): CommandResult {
            return CommandResult(true, "")
        }

        fun fail(msg: String): CommandResult {
            return CommandResult(false, msg)
        }
    }

    fun isSuccess(): Boolean {
        return success
    }

    fun getFailMessage(): String {
        return message
    }

}