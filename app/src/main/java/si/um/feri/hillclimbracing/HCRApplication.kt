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

        data = TrackCollection.generateDefault()
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