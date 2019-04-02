package me.fzzy.dair.util

import me.fzzy.dair.Bot
import me.fzzy.dair.Player

class Players {
    private val allPlayers = hashMapOf<Long, Player>()

    val all: Collection<Player>
        get() = allPlayers.values

    fun exists(id: Long): Boolean {
        return allPlayers.containsKey(id)
    }

    fun remove(player: Player) {
        remove(player.id)
    }

    fun remove(id: Long) {
        allPlayers.remove(id)
    }

    fun set(player: Player) {
        Bot.leaderboard.setValue(player.id, player.elo)
        allPlayers[player.id] = player
    }

    fun getOrDefault(id: Long, player: Player): Player {
        return allPlayers.getOrDefault(id, player)
    }

    fun getOrDefault(id: Long, name: String): Player {
        return allPlayers.getOrDefault(id, getDefaultPlayer(id, name))
    }

    fun get(id: Long): Player? {
        return allPlayers[id]
    }

    fun getDefaultPlayer(id: Long, name: String): Player {
        return Player(0, name, id, 1000f)
    }
}