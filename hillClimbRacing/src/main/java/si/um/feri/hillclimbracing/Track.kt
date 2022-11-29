package si.um.feri.hillclimbracing

import si.um.feri.hillclimbracing.enums.DifficultyEnum
import java.time.Duration
import java.util.*

class Track(var title: String, val difficulty: DifficultyEnum, var start: Point, var finish: Point) {
    val id: UUID = UUID.randomUUID()
    var leaderboard: MutableList<Score> = mutableListOf()

    private fun order() {
        leaderboard.sortedWith(compareBy({ Duration.between(it.startTime,it.finishTime) }, { it.finishTime }))
    }

    fun addScore(score: Score) {
        leaderboard.add(score)
        order()
    }

    fun removeScore(id: UUID) {
        leaderboard.removeAt(leaderboard.indexOfFirst { it.id == id })
    }
}