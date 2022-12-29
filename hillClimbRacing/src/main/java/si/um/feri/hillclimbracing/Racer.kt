package si.um.feri.hillclimbracing

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class Racer(
    var firstname: String,
    var lastname: String,
    var email: String,
    var birthdate: String
) : Serializable {
    val id: String = UUID.randomUUID().toString()

    constructor() : this("","","", "")
}