package si.um.feri.hillclimbracing

import si.um.feri.hillclimbracing.enums.DifficultyEnum
import java.io.Serializable
import java.time.Duration
import java.util.*

class Track(
    var title: String,
    var difficulty: DifficultyEnum,
    var start: Point,
    var finish: Point,
    var description: String = String()
) : Serializable {
    val id: UUID = UUID.randomUUID()
    var leaderboard: MutableList<Score> = mutableListOf()

    private fun order() {
        leaderboard.sortWith(compareBy<Score> {
            Duration.between(
                it.startTime,
                it.finishTime
            )
        }.thenByDescending { it.finishTime })
    }

    fun addScore(score: Score) {
        val index = leaderboard.indexOfFirst { it.racerId == score.racerId }
        if(index == -1) {
            leaderboard.add(score)
        }
        else {
            leaderboard[index] = score
        }
        order()
    }

    fun removeScore(id: UUID) {
        leaderboard.removeAt(leaderboard.indexOfFirst { it.id == id })
    }
}