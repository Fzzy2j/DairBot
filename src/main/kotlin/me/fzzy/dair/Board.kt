package me.fzzy.dair

import me.fzzy.dair.util.Leaderboard
import me.fzzy.dair.util.MatchSet

interface Board {
    val leaderboard: Leaderboard
    fun doMatches(set: MatchSet)
    fun clear()
}