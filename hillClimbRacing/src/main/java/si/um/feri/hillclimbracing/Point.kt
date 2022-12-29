package si.um.feri.hillclimbracing

import java.io.Serializable

class Point(val latitude: Double, val longitude: Double) : Serializable {
    constructor() : this(0.0,0.0)
}