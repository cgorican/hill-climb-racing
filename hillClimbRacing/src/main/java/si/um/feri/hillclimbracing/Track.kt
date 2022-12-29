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
    val id: String = UUID.randomUUID().toString()
    var leaderboard: MutableList<Score> = mutableListOf()

    constructor() : this("",DifficultyEnum.EASY,Point(0.0,0.0),Point(0.0,0.0),"")

    private fun order() {
        leaderboard.sortWith(compareBy<Score> {
            Duration.between(
                Score.toLocalDateTime(it.startTime),
                Score.toLocalDateTime(it.finishTime)
            )
        }.thenByDescending { Score.toLocalDateTime(it.finishTime) })
    }

    fun updateScore(score: Score) {
        val index = leaderboard.indexOfFirst { it.racerId == score.racerId }
        if(index == -1) {
            leaderboard.add(score)
        }
        else {
            leaderboard[index] = score
        }
        order()
    }

    fun removeScore(id: String) {
        leaderboard.removeAt(leaderboard.indexOfFirst { it.id == id })
    }
}