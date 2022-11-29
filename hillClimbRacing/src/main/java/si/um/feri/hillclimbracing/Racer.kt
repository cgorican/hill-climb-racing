package si.um.feri.hillclimbracing

import java.time.LocalDateTime
import java.util.*

class Racer(val firstname: String,
            val lastname: String,
            val email: String,
            val birthdate: LocalDateTime) {
    val id: UUID = UUID.randomUUID()
}