package si.um.feri.hillclimbracing

import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class Score(
    val racerId: String,
    val startTime: String,
    val finishTime: String,
) : Serializable, Comparable<Score> {
    val id: String = UUID.randomUUID().toString()

    constructor() : this("","", "")

    companion object {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

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

        fun toFormattedString(localDateTime: LocalDateTime): String {
            return localDateTime.format(formatter)
        }

        fun toLocalDateTime(localDateTimeString: String): LocalDateTime {
            return LocalDateTime.parse(localDateTimeString, formatter)
        }
    }

    override fun compareTo(other: Score): Int {
        val duration: Duration = Duration.between(toLocalDateTime(startTime), toLocalDateTime(finishTime))
        val durationOther: Duration = Duration.between(toLocalDateTime(other.startTime), toLocalDateTime(other.finishTime))
        var result: Int = (duration.toMillis() - durationOther.toMillis()).toInt()
        if (result == 0) {
            result = (finishTime.compareTo(other.finishTime))
        }
        return result
    }

    override fun toString(): String {
        val duration: Duration = Duration.between(toLocalDateTime(startTime), toLocalDateTime(finishTime))
        return durationToString(duration)
    }
}