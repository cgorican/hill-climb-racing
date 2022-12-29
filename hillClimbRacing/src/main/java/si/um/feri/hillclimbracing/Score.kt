package si.um.feri.hillclimbracing

import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

class Score(
    val racerId: String,
    val startTime: LocalDateTime,
    val finishTime: LocalDateTime
) : Serializable, Comparable<Score> {
    val id: String = UUID.randomUUID().toString()

    constructor() : this("",LocalDateTime.now(), LocalDateTime.now())

    companion object {
        fun durationToString(duration: Duration): String {
            val hrs = duration.toHours() % 24
            val min = duration.toMinutes() % 60
            val secs = (duration.toMillis() / 1000) % 60
            val millis = duration.toMillis() % 1000

            return if(hrs > 0) {
                String.format("%02d:%02d:%02d.%03d", hrs, min, secs, millis)
            } else {
                String.format("%02d:%02d.%03d", min, secs, millis)
            }
        }
    }

    override fun compareTo(other: Score): Int {
        val duration: Duration = Duration.between(startTime, finishTime)
        val durationOther: Duration = Duration.between(other.startTime, other.finishTime)
        var result: Int = (duration.toMillis() - durationOther.toMillis()).toInt()
        if (result == 0) {
            result = (finishTime.compareTo(other.finishTime))
        }
        return result
    }

    override fun toString(): String {
        val duration: Duration = Duration.between(startTime, finishTime)
        return durationToString(duration)
    }
}