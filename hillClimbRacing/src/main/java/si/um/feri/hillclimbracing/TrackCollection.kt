package si.um.feri.hillclimbracing

import si.um.feri.hillclimbracing.enums.DifficultyEnum
import java.util.*

class TrackCollection {
    var racer: Racer? = null
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
            result.addTrack(Track("Kalvarija",DifficultyEnum.EASY,
                Point(46.5693499,15.634215),
                Point(46.5708842,15.6356722),
                "Štenge"))
            result.addTrack(Track("Kalvarija",DifficultyEnum.EASY,
                Point(46.5693499,15.634215),
                Point(46.5690347,15.6397799)))
            result.addTrack(Track("Boč",DifficultyEnum.EASY,
                Point(46.297389, 15.582705),
                Point(46.289381, 15.599440),
                "Zgornje Poljčane, senčna pot"))
            result.addTrack(Track("Boč",DifficultyEnum.HARD,
                Point(46.297389, 15.582705),
                Point(46.289381, 15.599440),
                "Zgornje Poljčane, plezalna pot"))
            result.addTrack(Track("Boč",DifficultyEnum.EASY,
                Point(46.283045, 15.593963),
                Point(46.289381, 15.599440),
            "Planinski dom"))

            result.order()

            return result
        }
    }

    fun order() {
        tracks.sortWith(compareBy<Track> {it.title}.thenBy { it.difficulty.value })
    }

    fun addTrack(track: Track) {
        tracks.add(track)
        order()
    }

    fun updateTrack(track: Track) {
        val index = tracks.indexOfFirst { it.id == track.id }
        if(index != -1) {
            tracks[index] = track
            order()
        }
        else addTrack(track)
    }

    fun removeTrack(id: UUID) {
        tracks.removeAt(tracks.indexOfFirst { it.id == id })
    }

    fun setTrackScore(trackId: UUID, score: Score) {
        tracks[tracks.indexOfFirst { it.id == trackId }].addScore(score)
    }
}