package me.fzzy.dair.boards

import me.fzzy.dair.Board
import me.fzzy.dair.Match
import me.fzzy.dair.util.Leaderboard
import me.fzzy.dair.util.MatchSet

class EloBoard : Board {
    override val leaderboard = Leaderboard()
    private val defaultScore = 1000f

    private val K = 100

    override fun doMatches(set: MatchSet) {
        for (match in set.matches) {
            doMatch(match)
        }
    }

    fun doMatch(match: Match) {
        var winnerElo = leaderboard.getScore(match.winner)?: defaultScore
        var loserElo = leaderboard.getScore(match.loser)?: defaultScore

        val p1 = probability(winnerElo, loserElo)
        val p2 = probability(loserElo, winnerElo)

        winnerElo += K * (1 - p2)
        loserElo += K * (0 - p1)

        leaderboard.setValue(match.winner, winnerElo)
        leaderboard.setValue(match.loser, loserElo)
    }

    private fun probability(rating1: Float, rating2: Float): Float {
        return 1f * 1f / (1 + 1f * Math.pow(10.0, 1.0 * (rating1 - rating2) / 400).toFloat())
    }

    override fun clear() {
        leaderboard.clear()
    }
}