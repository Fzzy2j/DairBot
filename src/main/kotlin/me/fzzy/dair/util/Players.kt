package me.fzzy.dair.util

import me.fzzy.dair.Player

class Players {
    private val allPlayers = hashMapOf<Long, LivePlayer>()

    val all: Collection<LivePlayer>
        get() = allPlayers.values

    fun exists(id: Long): Boolean {
        return allPlayers.containsKey(id)
    }

    fun remove(player: LivePlayer) {
        remove(player.p.id)
    }

    fun remove(id: Long) {
        allPlayers.remove(id)
    }

    fun set(player: LivePlayer) {
        allPlayers[player.p.id] = player
    }

    fun getOrCreate(player: Player): LivePlayer {
        if (!allPlayers.containsKey(player.id))
            allPlayers[player.id] = getDefaultPlayer(player)
        return allPlayers[player.id]!!
    }

    fun get(id: Long): LivePlayer? {
        return allPlayers[id]
    }

    fun getDefaultPlayer(player: Player): LivePlayer {
        return LivePlayer(player, 1000f, 0)
    }

    class LivePlayer(val p: Player, var elo: Float, var wins: Int)
}