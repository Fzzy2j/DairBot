package me.fzzy.dair.util

import me.fzzy.dair.Player

object Elo {

    const val K = 100

    fun doMatch(player1: Player, player2: Player, player1Wins: Boolean) {
        val p1 = probability(player1.elo, player2.elo)
        val p2 = probability(player2.elo, player1.elo)

        if (player1Wins) {
            player1.elo = player1.elo + K * (1 - p2)
            player2.elo = player2.elo + K * (0 - p1)
        } else {
            player1.elo = player1.elo + K * (0 - p2)
            player2.elo = player2.elo + K * (1 - p1)
        }
    }

    fun probability(rating1: Float, rating2: Float): Float {
        return 1f * 1f / (1 + 1f * Math.pow(10.0, 1.0 * (rating1 - rating2) / 400).toFloat())
    }
}