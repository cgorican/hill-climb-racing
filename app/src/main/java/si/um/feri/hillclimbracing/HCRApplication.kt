package si.um.feri.hillclimbracing

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.util.*

class HCRApplication : Application(), DefaultLifecycleObserver {
    private val TAG = HCRApplication::class.qualifiedName
    private lateinit var gson: Gson
    private lateinit var file: File

    var mainActivity: MainActivity? = null

    var notificationsEnabled = false
    var location: Point? = null
    lateinit var data: TrackCollection
    val racers: MutableList<Racer> = mutableListOf()

    lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor


    val id: UUID = UUID.randomUUID()


    /*
    //val options = FirebaseOptions.fromResource(this)
    val options = FirebaseOptions.Builder()
        .setProjectId("hillclimbracing-38b2e")
        .setApplicationId("1:278564917692:android:951056ea8eec24e19e9bb9")
        .setApiKey("AIzaSyBUpqyDukJXqmX0__nvkyuZD9ffpZyEr1A")
        .build()
     */
    // realtime (depression base) database (firebase)
    //private lateinit var database: FirebaseDatabase
    lateinit var database: DatabaseReference
    lateinit var refTracks: DatabaseReference
    lateinit var refRacers: DatabaseReference

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        sharedPref = getSharedPreferences(getString(R.string.SHR_PREF_PATH), Context.MODE_PRIVATE)
        editor = sharedPref.edit()
        setSessionId()

        gson = Gson()
        /*
        file = File(filesDir, getString(R.string.json_data_path))
        if(file.exists()) {
            readJsonFile()
        }
        */


        /**
         * Generate default data
         *
         * data = TrackCollection.generateDefault()
         * for(track in data.tracks) {
         *  refTracks.child(track.id.toString())
         *           .setValue(track)
         * }
         */
        data = TrackCollection()

        // init firebase
        //FirebaseApp.initializeApp(this, options)
        //database = FirebaseDatabase.getInstance()
        database = Firebase.database.reference
        refTracks = database.child("tracks")
        refRacers = database.child("racers")

        // listens for events and retrieves data
        initDatabaseListeners()

        // create notification channel
        val channel = NotificationChannel(
            getString(R.string.NOTIFICATIONS_CHANNEL_ID),
            getString(R.string.app_name)+" notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun initDatabaseListeners() {
        // tracks
        refTracks.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                data.tracks.clear()
                for (sps in snapshot.children) {
                    val track = sps.getValue<Track>()
                    if (track != null) {
                        data.addTrack(track)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // failed to read/write
                Log.w(TAG, "Database error - failed to read value")
            }
        })
        refTracks.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val dbTrack = snapshot.getValue<Track>()
                if(dbTrack != null) {
                    Log.i(TAG, "TrackAdded: "+dbTrack.toString())
                    mainActivity?.displayNotification(dbTrack)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // :)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // :)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // idk
            }

            override fun onCancelled(error: DatabaseError) {
                // failed to read/write
                Log.w(TAG, "Database error")
            }


        })

        // users
        refRacers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                racers.clear()
                for (sps in snapshot.children) {
                    val racer = sps.getValue<Racer>()
                    if (racer != null) {
                        racers.add(racer)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // failed to read/write
                Log.w(TAG, "Database error")
            }
        })
    }

    // fun saveData() = writeJsonFile()

    private fun readJsonFile() {
        val fileData: TrackCollection? = deserializeJSONFile()
        if(fileData != null) data = fileData
    }

    private fun writeJsonFile() {
        val jsonString: String? = serialize(data)
        if(jsonString != null) {
            file.writeText(jsonString)
        }
    }

    private fun serialize(value: Any): String? {
        val gson = GsonBuilder().create()
        var jsonString: String? = null
        try {
            jsonString = gson.toJson(data)
        }
        catch(e: Exception) {
            Log.e(TAG, "Serialization failed")
        }
        return jsonString
    }

    private fun deserializeJSONFile(): TrackCollection? {
        if(!file.exists()) return null
        val fileReader = FileReader(file)
        var carCollection: TrackCollection? = null
        try {
            carCollection = gson.fromJson(fileReader, TrackCollection::class.java)
        }
        catch(e: Exception) {
            Log.e(TAG, "Deserialization failed")
        }
        return carCollection
    }

    private fun setSessionId() {
        editor.putString(getString(R.string.SHR_PREF_SESSION_ID),id.toString())
        editor.apply()
    }

    // Firebase operations
    // Tracks
    fun addTrack(track: Track) {
        val trackRef = refTracks.child(track.id.toString())
        trackRef.setValue(track)
    }

    fun putTrack(track: Track) {
        val trackRef = refTracks.child(track.id.toString())
        trackRef.setValue(track)
    }

    fun addTrackScore(trackId: String, score: Score) {
        val index = data.tracks.indexOfFirst { it.id == trackId }
        if(index == -1) return
        data.addTrackScore(trackId,score)
        putTrack(data.tracks[index])
    }

    fun deleteTrack(trackId: String) {
        refTracks.child(trackId).removeValue()
    }

    // Racers
    fun updateRacer(): Boolean {
        if(data.racer == null) return false
        refRacers.child(data.racer!!.id)
            .setValue(data.racer!!)
        return true
    }

    fun recoverUserBySharedPrefs() {
        // check db for shared pref id
        Log.i(TAG,"RecoveringUserProfile")
        if(data.racer == null) {
            val racerIdShrPref = sharedPref
                .getString(getString(R.string.SHR_PREF_RACER_ID), null)
            Log.i(TAG,"SHARED_PREF_RACER_ID: $racerIdShrPref")
            Log.i(TAG, "${racers.size} racers present")
            if(racerIdShrPref != null && racers.isNotEmpty()) {
                val index = racers.indexOfFirst { it.id == racerIdShrPref }
                if(index > -1) {
                    data.racer = racers[index]
                }
                Log.i(TAG,data.racer.toString())
            }
        }
    }
}