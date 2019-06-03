package me.fzzy.dair.boards

import me.fzzy.dair.Board
import me.fzzy.dair.Match
import me.fzzy.dair.boards.glicko2.Rating
import me.fzzy.dair.boards.glicko2.RatingCalculator
import me.fzzy.dair.boards.glicko2.RatingPeriodResults
import me.fzzy.dair.util.Leaderboard
import me.fzzy.dair.util.MatchSet

class Glicko2Board : Board {
    override val leaderboard = Leaderboard()

    val glickoPlayers = hashMapOf<String, Rating>()
    private var ratingCalc = RatingCalculator()
    private var period = RatingPeriodResults()

    override fun doMatches(set: MatchSet) {
        if (set.link.contains("dair")) return
        for (match in set.matches) {
            val winnerRating = glickoPlayers.getOrDefault(match.winner, Rating(ratingCalc))
            val loserRating = glickoPlayers.getOrDefault(match.loser, Rating(ratingCalc))
            period.addResult(winnerRating, loserRating)
            glickoPlayers[match.winner] = winnerRating
            glickoPlayers[match.loser] = loserRating
        }
        ratingCalc.updateRatings(period)

        for ((name, rating) in glickoPlayers) {
            leaderboard.setValue(name, (rating.glicko2Rating - rating.glicko2RatingDeviation * 2).toFloat())
        }
    }

    override fun clear() {
        leaderboard.clear()
        ratingCalc = RatingCalculator()
        period = RatingPeriodResults()
        glickoPlayers.clear()
    }
}