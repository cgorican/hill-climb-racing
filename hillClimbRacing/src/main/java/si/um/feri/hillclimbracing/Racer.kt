package si.um.feri.hillclimbracing

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class Racer(
    val firstname: String,
    val lastname: String,
    var email: String,
    val birthdate: LocalDate
) : Serializable {
    val id: UUID = UUID.randomUUID()
}