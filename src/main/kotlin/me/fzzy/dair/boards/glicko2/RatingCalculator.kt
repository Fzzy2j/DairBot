package me.fzzy.dair.boards.glicko2

/**
 * This is the main calculation engine based on the contents of Glickman's paper.
 * http://www.glicko2.net/glicko2/glicko2.pdf
 *
 * @author Jeremy Gooch
 */
class RatingCalculator {

    companion object {

        const val DEFAULT_RATING = 1500.0
        const val DEFAULT_DEVIATION = 250.0
        const val DEFAULT_VOLATILITY = 0.06
        const val DEFAULT_TAU = 0.75
        const val MULTIPLIER = 173.7178
        const val CONVERGENCE_TOLERANCE = 0.000001


        /**
         * Converts from the value used within the algorithm to a rating in the same range as traditional Elo et al
         *
         * @param rating in Glicko2 scale
         * @return rating in glicko2 scale
         */
        fun convertRatingToOriginalGlickoScale(rating: Double): Double {
            return rating * MULTIPLIER + DEFAULT_RATING
        }


        /**
         * Converts from a rating in the same range as traditional Elo et al to the value used within the algorithm
         *
         * @param rating in glicko2 scale
         * @return rating in Glicko2 scale
         */
        fun convertRatingToGlicko2Scale(rating: Double): Double {
            return (rating - DEFAULT_RATING) / MULTIPLIER
        }


        /**
         * Converts from the value used within the algorithm to a rating deviation in the same range as traditional Elo et al
         *
         * @param ratingDeviation in Glicko2 scale
         * @return ratingDeviation in glicko2 scale
         */
        fun convertRatingDeviationToOriginalGlickoScale(ratingDeviation: Double): Double {
            return ratingDeviation * MULTIPLIER
        }


        /**
         * Converts from a rating deviation in the same range as traditional Elo et al to the value used within the algorithm
         *
         * @param ratingDeviation in glicko2 scale
         * @return ratingDeviation in Glicko2 scale
         */
        fun convertRatingDeviationToGlicko2Scale(ratingDeviation: Double): Double {
            return ratingDeviation / MULTIPLIER
        }
    }

    var tau: Double
    var defaultVolatility: Double

    var defaultRating: Double
    var defaultRatingDeviation: Double

    init {
        tau = DEFAULT_TAU
        defaultVolatility = DEFAULT_VOLATILITY
        defaultRating = DEFAULT_RATING
        defaultRatingDeviation = DEFAULT_DEVIATION
    }


    /**
     *
     * Run through all players within a resultset and calculate their new ratings.
     *
     * Players within the resultset who did not compete during the rating period
     * will have see their deviation increase (in line with Prof Glickman's paper).
     *
     * Note that this method will clear the results held in the association resultset.
     *
     * @param results
     */
    fun updateRatings(results: RatingPeriodResults) {
        for (player in results.getParticipants()) {
            if (results.getResults(player).count() > 0) {
                calculateNewRating(player, results.getResults(player))
            } else {
                // if a player does not compete during the rating period, then only Step 6 applies.
                // the player's rating and volatility parameters remain the same but deviation increases
                player.setWorkingRating(player.glicko2Rating)
                player.setWorkingRatingDeviation(
                    calculateNewRD(
                        player.glicko2RatingDeviation,
                        player.volatility
                    )
                )
                player.setWorkingVolatility(player.volatility)
            }
        }

        // now iterate through the participants and confirm their new ratings
        for (player in results.getParticipants()) {
            player.finaliseRating()
        }

        // lastly, clear the result set down in anticipation of the next rating period
        results.clear()
    }


    /**
     * This is the function processing described in step 5 of Glickman's paper.
     *
     * @param player
     * @param results
     */
    private fun calculateNewRating(player: Rating, results: List<Result>) {
        val phi = player.glicko2RatingDeviation
        val sigma = player.volatility
        val a = Math.log(Math.pow(sigma, 2.0))
        val delta = delta(player, results)
        val v = v(player, results)

        // step 5.2 - set the initial values of the iterative algorithm to come in step 5.4
        var A = a
        var B = 0.0
        if (Math.pow(delta, 2.0) > Math.pow(phi, 2.0) + v) {
            B = Math.log(Math.pow(delta, 2.0) - Math.pow(phi, 2.0) - v)
        } else {
            var k = 1.0
            B = a - k * Math.abs(tau)

            while (f(B, delta, phi, v, a, tau) < 0) {
                k++
                B = a - k * Math.abs(tau)
            }
        }

        // step 5.3
        var fA = f(A, delta, phi, v, a, tau)
        var fB = f(B, delta, phi, v, a, tau)

        // step 5.4
        while (Math.abs(B - A) > CONVERGENCE_TOLERANCE) {
            val C = A + (A - B) * fA / (fB - fA)
            val fC = f(C, delta, phi, v, a, tau)

            if (fC * fB < 0) {
                A = B
                fA = fB
            } else {
                fA = fA / 2.0
            }

            B = C
            fB = fC
        }

        val newSigma = Math.exp(A / 2.0)

        player.setWorkingVolatility(newSigma)

        // Step 6
        val phiStar = calculateNewRD(phi, newSigma)

        // Step 7
        val newPhi = 1.0 / Math.sqrt(1.0 / Math.pow(phiStar, 2.0) + 1.0 / v)

        // note that the newly calculated rating values are stored in a "working" area in the Rating object
        // this avoids us attempting to calculate subsequent participants' ratings against a moving target
        player.setWorkingRating(
            player.glicko2Rating + Math.pow(newPhi, 2.0) * outcomeBasedRating(player, results)
        )
        player.setWorkingRatingDeviation(newPhi)
        player.incrementNumberOfResults(results.size)
    }

    private fun f(x: Double, delta: Double, phi: Double, v: Double, a: Double, tau: Double): Double {
        return Math.exp(x) * (Math.pow(delta, 2.0) - Math.pow(phi, 2.0) - v - Math.exp(x)) / (2.0 * Math.pow(
            Math.pow(
                phi,
                2.0
            ) + v + Math.exp(x), 2.0
        )) - (x - a) / Math.pow(tau, 2.0)
    }


    /**
     * This is the first sub-function of step 3 of Glickman's paper.
     *
     * @param deviation
     * @return
     */
    private fun g(deviation: Double): Double {
        return 1.0 / Math.sqrt(1.0 + 3.0 * Math.pow(deviation, 2.0) / Math.pow(Math.PI, 2.0))
    }


    /**
     * This is the second sub-function of step 3 of Glickman's paper.
     *
     * @param playerRating
     * @param opponentRating
     * @param opponentDeviation
     * @return
     */
    private fun E(playerRating: Double, opponentRating: Double, opponentDeviation: Double): Double {
        return 1.0 / (1.0 + Math.exp(-1.0 * g(opponentDeviation) * (playerRating - opponentRating)))
    }


    /**
     * This is the main function in step 3 of Glickman's paper.
     *
     * @param player
     * @param results
     * @return
     */
    private fun v(player: Rating, results: List<Result>): Double {
        var v = 0.0

        for (result in results) {
            v += (Math.pow(g(result.getOpponent(player).glicko2RatingDeviation), 2.0)
                    * E(
                player.glicko2Rating,
                result.getOpponent(player).glicko2Rating,
                result.getOpponent(player).glicko2RatingDeviation
            )
                    * (1.0 - E(
                player.glicko2Rating,
                result.getOpponent(player).glicko2Rating,
                result.getOpponent(player).glicko2RatingDeviation
            )))
        }

        return Math.pow(v, -1.0)
    }


    /**
     * This is a formula as per step 4 of Glickman's paper.
     *
     * @param player
     * @param results
     * @return delta
     */
    private fun delta(player: Rating, results: List<Result>): Double {
        return v(player, results) * outcomeBasedRating(player, results)
    }


    /**
     * This is a formula as per step 4 of Glickman's paper.
     *
     * @param player
     * @param results
     * @return expected rating based on game outcomes
     */
    private fun outcomeBasedRating(player: Rating, results: List<Result>): Double {
        var outcomeBasedRating = 0.0

        for (result in results) {
            outcomeBasedRating += g(result.getOpponent(player).glicko2RatingDeviation) * (result.getScore(player) - E(
                player.glicko2Rating,
                result.getOpponent(player).glicko2Rating,
                result.getOpponent(player).glicko2RatingDeviation
            ))
        }

        return outcomeBasedRating
    }


    /**
     * This is the formula defined in step 6. It is also used for players
     * who have not competed during the rating period.
     *
     * @param phi
     * @param sigma
     * @return new rating deviation
     */
    private fun calculateNewRD(phi: Double, sigma: Double): Double {
        return Math.sqrt(Math.pow(phi, 2.0) + Math.pow(sigma, 2.0))
    }
}