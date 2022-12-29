package si.um.feri.hillclimbracing

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
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
    private val racers: MutableList<Racer> = mutableListOf()

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

        sharedPref = getSharedPreferences(getString(R.string.path_shr_pref), Context.MODE_PRIVATE)
        editor = sharedPref.edit()
        setSessionId()

        // init firebase
        //FirebaseApp.initializeApp(this, options)
        //database = FirebaseDatabase.getInstance()
        database = Firebase.database.reference
        refTracks = database.child("tracks")
        refRacers = database.child("racers")


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
                        data.tracks.add(track)
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
                val dbRacers: String? = snapshot.getValue(String::class.java)
                Log.i(TAG, "Racers: "+dbRacers.toString())
                /*
                if(dbRacers != null) {
                    val gson = Gson()

                    var racersList: MutableList<Racer>? = null
                    try {
                        val listType = object : TypeToken<MutableList<Racer>>() {}.type
                        racersList = gson.fromJson(dbRacers, listType)
                    }
                    catch(e: Exception) {
                        Log.e(TAG, "Deserialization failed - MutableList<Racer>")
                    }
                    if(racersList != null) {
                        racers.clear()
                        racers.addAll(racersList)
                    }
                }
                 */
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
        editor.putString(getString(R.string.shr_pref_session_id),id.toString())
        editor.apply()
    }

    // Firebase operations
    // Tracks
    fun addTrack(track: Track) {
        // data.addTrack(track) -maybe we dont need it
        val trackRef = refTracks.child(track.id.toString())
        trackRef.setValue(track)
    }

    fun putTrack(track: Track) {
        val trackRef = refTracks.child(track.id.toString())
        trackRef.setValue(track)
        // data.updateTrack(track) -maybe we dont need it
    }

    fun addTrackScore(trackId: String, score: Score) {
        // data.addTrackScore(trackId,score) -maybe we dont need it
    }

    fun deleteTrack(trackId: String) {
        // data.deleteTrack(trackId) -maybe we dont need it
    }

    // Racers
    fun updateRacer(): Boolean {
        if(data.racer == null) return false
        when(val index = racers.indexOfFirst { it.id == data.racer!!.id }) {
            -1 -> racers.add(data.racer!!)
            else -> racers[index] = data.racer!!
        }
        return true
    }
}