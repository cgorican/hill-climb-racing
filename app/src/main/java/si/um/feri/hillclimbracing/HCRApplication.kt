package si.um.feri.hillclimbracing

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.util.*

class HCRApplication : Application(), DefaultLifecycleObserver {
    private val TAG = HCRApplication::class.qualifiedName
    private lateinit var gson: Gson
    private lateinit var file: File

    lateinit var data: TrackCollection
    lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    val id: UUID = UUID.randomUUID()

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        sharedPref = getSharedPreferences(getString(R.string.path_shr_pref), Context.MODE_PRIVATE)
        editor = sharedPref.edit()
        setSessionId()

        gson = Gson()
        file = File(filesDir, getString(R.string.json_data_path))
        if(file.exists()) {
            readJsonFile()
        }

        data = TrackCollection.generateDefault()
    }

    private fun readJsonFile() {
        val fileData: TrackCollection? = deserialize()
        if(fileData != null) data = fileData
    }

    private fun writeJsonFile() {
        val jsonString: String? = serialize(data)
        if(jsonString != null) {
            file.writeText(jsonString)
        }
    }

    fun saveData() = writeJsonFile()

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

    private fun deserialize(): TrackCollection? {
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

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        // track location - on
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        // track location - off
    }
}