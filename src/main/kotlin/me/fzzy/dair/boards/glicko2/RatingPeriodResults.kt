package me.fzzy.dair.boards.glicko2

import java.util.ArrayList
import java.util.HashSet

/**
 * This class holds the results accumulated over a rating period.
 *
 * @author Jeremy Gooch
 */
class RatingPeriodResults constructor(private val participants: HashSet<Rating> = HashSet()) {
    private val results = ArrayList<Result>()

    /**
     * Add a result to the set.
     *
     * @param winner
     * @param loser
     */
    fun addResult(winner: Rating, loser: Rating) {
        if (!participants.contains(winner)) participants.add(winner)
        if (!participants.contains(loser)) participants.add(loser)
        val result = Result(winner, loser)

        results.add(result)
    }


    /**
     * Record a draw between two players and add to the set.
     *
     * @param player1
     * @param player2
     */
    fun addDraw(player1: Rating, player2: Rating) {
        if (!participants.contains(player1)) participants.add(player1)
        if (!participants.contains(player2)) participants.add(player2)
        val result = Result(player1, player2, true)

        results.add(result)
    }


    /**
     * Get a list of the results for a given player.
     *
     * @param player
     * @return List of results
     */
    fun getResults(player: Rating): List<Result> {
        val filteredResults = ArrayList<Result>()

        for (result in results) {
            if (result.participated(player)) {
                filteredResults.add(result)
            }
        }

        return filteredResults
    }


    /**
     * Get all the participants whose results are being tracked.
     *
     * @return set of all participants covered by the resultset.
     */
    fun getParticipants(): Set<Rating> {
        // Run through the results and make sure all players have been pushed into the participants set.
        for (result in results) {
            participants.add(result.winner)
            participants.add(result.loser)
        }

        return participants
    }


    /**
     * Clear the resultset.
     */
    fun clear() {
        results.clear()
    }
}