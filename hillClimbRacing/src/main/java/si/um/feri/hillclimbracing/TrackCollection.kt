package si.um.feri.hillclimbracing

import si.um.feri.hillclimbracing.enums.DifficultyEnum
import java.util.*

class TrackCollection {
    lateinit var racer: Racer
    val tracks = mutableListOf<Track>()

    companion object {
        @JvmStatic
        fun generateDefault(): TrackCollection {
            val result = TrackCollection()


            result.addTrack(Track("Piramida",DifficultyEnum.EASY,
                Point(46.567693, 15.649105),
                Point(46.568250, 15.652314)))
            result.addTrack(Track("Trikotna jasa",DifficultyEnum.EASY,
                Point(46.5317145,15.6029794),
                Point(46.5252336,15.6014253)))
            result.addTrack(Track("Koča Luka",DifficultyEnum.EASY,
                Point(46.5317145,15.6029794),
                Point(46.5163247,15.5851272)))
            result.addTrack(Track("Bellevue",DifficultyEnum.MEDIUM,
                Point(46.5317145,15.6029794),
                Point(46.515749, 15.579491)))
            result.addTrack(Track("Kalvarija (Štenge)",DifficultyEnum.EASY,
                Point(46.5693499,15.634215),
                Point(46.5708842,15.6356722)))
            result.addTrack(Track("Kalvarija",DifficultyEnum.EASY,
                Point(46.5693499,15.634215),
                Point(46.5690347,15.6397799)))

            result.order()

            return result
        }
    }

    fun order() {
        tracks.sortedWith(compareBy({it.title},{it.difficulty.value}))
    }

    fun addTrack(track: Track) {
        tracks.add(track)
        order()
    }

    fun removeTrack(id: UUID) {
        tracks.removeAt(tracks.indexOfFirst { it.id == id })
    }
}