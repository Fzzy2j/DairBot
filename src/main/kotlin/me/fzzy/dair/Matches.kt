package me.fzzy.dair

class Matches {

    val matches = arrayListOf<Match>()
    val deletedIds = arrayListOf<Long>()

    class Match(val player1: Player, val player2: Player, val player1Wins: Boolean) {
        private val K = 100

        fun doMatch() {
            val lp1 = Bot.players.getOrCreate(player1)
            val lp2 = Bot.players.getOrCreate(player2)

            val p1 = probability(lp1.elo, lp2.elo)
            val p2 = probability(lp2.elo, lp1.elo)

            if (player1Wins) {
                lp1.elo = lp1.elo + K * (1 - p2)
                lp2.elo = lp2.elo + K * (0 - p1)

                lp1.wins++
            } else {
                lp1.elo = lp1.elo + K * (0 - p2)
                lp2.elo = lp2.elo + K * (1 - p1)

                lp2.wins++
            }

            Bot.players.set(lp1)
            Bot.players.set(lp2)
        }

        fun probability(rating1: Float, rating2: Float): Float {
            return 1f * 1f / (1 + 1f * Math.pow(10.0, 1.0 * (rating1 - rating2) / 400).toFloat())
        }

    }

}