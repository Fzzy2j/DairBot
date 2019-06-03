package me.fzzy.dair.boards.glicko2

/**
 * Represents the result of a match between two players.
 *
 * @author Jeremy Gooch
 */
class Result {

    companion object {
        private const val POINTS_FOR_WIN = 1.0
        private const val POINTS_FOR_LOSS = 0.0
        private const val POINTS_FOR_DRAW = 0.5
    }

    private var isDraw = false
    var winner: Rating
        private set
    var loser: Rating
        private set


    /**
     * Record a new result from a match between two players.
     *
     * @param winner
     * @param loser
     */
    constructor(winner: Rating, loser: Rating) {
        if (!validPlayers(winner, loser)) {
            throw IllegalArgumentException()
        }

        this.winner = winner
        this.loser = loser
    }


    /**
     * Record a draw between two players.
     *
     * @param player1
     * @param player2
     * @param isDraw (must be set to "true")
     */
    constructor(player1: Rating, player2: Rating, isDraw: Boolean) {
        if (!isDraw || !validPlayers(player1, player2)) {
            throw IllegalArgumentException()
        }

        this.winner = player1
        this.loser = player2
        this.isDraw = true
    }


    /**
     * Check that we're not doing anything silly like recording a match with only one player.
     *
     * @param player1
     * @param player2
     * @return
     */
    private fun validPlayers(player1: Rating, player2: Rating): Boolean {
        return player1 != player2
    }


    /**
     * Test whether a particular player participated in the match represented by this result.
     *
     * @param player
     * @return boolean (true if player participated in the match)
     */
    fun participated(player: Rating): Boolean {
        return winner == player || loser == player
    }


    /**
     * Returns the "score" for a match.
     *
     * @param player
     * @return 1 for a win, 0.5 for a draw and 0 for a loss
     * @throws IllegalArgumentException
     */
    @Throws(IllegalArgumentException::class)
    fun getScore(player: Rating): Double {
        var score: Double

        score = when {
            winner == player -> POINTS_FOR_WIN
            loser == player -> POINTS_FOR_LOSS
            else -> throw IllegalArgumentException("Player did not participate in match")
        }

        if (isDraw) {
            score = POINTS_FOR_DRAW
        }

        return score
    }


    /**
     * Given a particular player, returns the opponent.
     *
     * @param player
     * @return opponent
     */
    fun getOpponent(player: Rating): Rating {
        return when {
            winner == player -> loser
            loser == player -> winner
            else -> throw IllegalArgumentException("Player did not participate in match")
        }
    }
}