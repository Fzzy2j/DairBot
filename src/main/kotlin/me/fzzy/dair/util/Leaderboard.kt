package me.fzzy.dair.util

class Leaderboard {

    private val valueMap: HashMap<String, PlayerData> = hashMapOf()
    private val rankMap: HashMap<Int, String> = hashMapOf()

    fun getAtRank(rank: Int): String? {
        return rankMap[rank]
    }

    fun getRank(name: String): Int? {
        return valueMap[name]?.rank
    }

    fun getScore(name: String): Float? {
        return valueMap[name]?.score
    }

    fun getScore(rank: Int): Float? {
        if (!rankMap.containsKey(rank)) return null
        return valueMap[rankMap[rank]!!]?.score
    }

    fun size(): Int {
        return rankMap.count()
    }

    fun clear() {
        rankMap.clear()
        valueMap.clear()
    }

    fun setValue(name: String, score: Float) {
        if (valueMap.containsKey(name)) {
            val prevValue = valueMap[name]!!.score

            // Determines if they need to move up or down in the leaderboard
            return if (score < prevValue) {
                moveDownInLeaderboard(name, score)
            } else {
                moveUpInLeaderboard(name, score)
            }
        } else {
            newEntry(name, score)
        }
    }

    private fun newEntry(name: String, score: Float) {

        // Start from the bottom of the leaderboard
        val rank = valueMap.size + 1
        valueMap[name] = PlayerData(rank, score)
        rankMap[rank] = name
        setValue(name, score)
    }

    private fun moveUpInLeaderboard(name: String, score: Float) {
        var rank = valueMap[name]!!.rank

        // If the new value is greater than the entry 1 rank above it, move it, repeat
        var compare = rankMap[rank - 1]
        while (rank != 1 && score > valueMap[compare]!!.score) {

            valueMap[compare!!] = PlayerData(rank, valueMap[compare]!!.score)
            rankMap[rank] = compare
            rank--
            rankMap[rank] = name
            compare = rankMap[rank - 1]
        }
        valueMap[name] = PlayerData(rank, score)
    }

    private fun moveDownInLeaderboard(name: String, score: Float) {
        var rank = valueMap[name]!!.rank

        // If the new value is less than the entry 1 rank below it, move it, repeat
        var compare = rankMap[rank + 1]
        while (rank != valueMap.size && score < valueMap[compare]!!.score) {

            valueMap[compare!!] = PlayerData(rank, valueMap[compare]!!.score)
            rankMap[rank] = compare
            rank++
            rankMap[rank] = name
            compare = rankMap[rank + 1]
        }
        valueMap[name] = PlayerData(rank, score)
    }

    private class PlayerData constructor(val rank: Int, val score: Float)

}