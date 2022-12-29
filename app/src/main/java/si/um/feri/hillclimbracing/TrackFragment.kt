package si.um.feri.hillclimbracing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import si.um.feri.hillclimbracing.databinding.FragmentTrackBinding
import si.um.feri.hillclimbracing.enums.DifficultyEnum
import si.um.feri.hillclimbracing.services.TimerService
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class TrackFragment : Fragment() {
    private val TAG = TrackFragment::class.qualifiedName
    private lateinit var app: HCRApplication
    private lateinit var start: LocalDateTime
    private lateinit var track: Track

    // auto-stop after 8h
    private val msUntilAutoStop: Long = 8 * 60 * 60 * 1000
    private lateinit var timerServiceIntent: Intent

    private var _binding: FragmentTrackBinding? = null
    private val args: TrackInputFragmentArgs by navArgs()
    private var end: LocalDateTime = LocalDateTime.MIN
    private var running = false
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss SSS")

    private lateinit var timerUpdateReceiver: BroadcastReceiver
    private lateinit var timerTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTrackBinding.inflate(inflater, container, false)

        timerServiceIntent = Intent(activity, TimerService::class.java)
        timerServiceIntent.putExtra(getString(R.string.TIMER_SERVICE_VAR), msUntilAutoStop)

        timerTextView = _binding!!.timer
        timerUpdateReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Get the time remaining from the intent
                var timerValue = intent?.getLongExtra(getString(R.string.TIMER_VALUE), 0) ?: 0
                if(timerValue < 0) {
                    timerValue = 0
                    timerTextView.text = Score.durationToString(Duration.ZERO)
                }
                else {
                    timerTextView.text = Score.durationToString(Duration.ofMillis(timerValue))
                }
            }
        }

        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireContext().applicationContext as HCRApplication

        track = app.data.tracks[args.index]

        // difficulty bubble
        _binding!!.displayDifficulty.text = track.difficulty.value.toString()
        when (track.difficulty.value) {
            DifficultyEnum.HARD.value -> _binding!!.displayDifficulty.background.setTint(requireContext().getColor(R.color.diff_hard))
            DifficultyEnum.MEDIUM.value -> _binding!!.displayDifficulty.background.setTint(requireContext().getColor(R.color.diff_medium))
            else -> _binding!!.displayDifficulty.background.setTint(requireContext().getColor(R.color.diff_easy))
        }

        // Display banner
        Picasso.get()
            .load("https://www.liveabout.com/thmb/3hOYoLBcmnd5Rd_JRCSSZoIlE44=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/MontBlancRegion_BuenaVistaImages_Getty1-56a16aee3df78cf7726a89cf.jpg")
            .placeholder(R.drawable.ic_question_mark)
            .error(R.drawable.ic_question_mark)
            .fit() // fit image into imageView
            .centerCrop()   //.centerInside()
            .noFade() // disable fade animation
            .into(_binding!!.banner)


        // timer display
        timerTextView.text = Score.durationToString(Duration.ZERO)
        timerTextView.setTextColor(requireContext().getColor(R.color.gray))
        // reset timer button
        _binding!!.btnTimerReset.setBackgroundColor(requireContext().getColor(R.color.light_gray))
        _binding!!.btnTimerReset.isEnabled = false

        _binding!!.btnTimerReset.setOnClickListener {
            if (!running && end != LocalDateTime.MIN) {
                end = LocalDateTime.MIN
                timerTextView.text = Score.durationToString(Duration.ZERO)
                timerTextView.setTextColor(requireContext().getColor(R.color.gray))
                _binding!!.btnTimerReset.setBackgroundColor(requireContext().getColor(R.color.light_gray))
                _binding!!.btnSubmit.visibility = View.INVISIBLE
                _binding!!.btnTimerReset.isEnabled = false
            }
        }

        // start/stop timer
        _binding!!.timer.setOnClickListener {
            if (running) {   // STOP
                trackEnd()
            } else if (end == LocalDateTime.MIN) { // START
                trackStart()
            }
        }

        // submit button
        _binding!!.btnSubmit.visibility = View.INVISIBLE
        _binding!!.btnSubmit.setOnClickListener {
            if(submitScore()) {
                Navigation.findNavController(view)
                    .navigate(R.id.action_trackFragment_to_mainFragment)
            }
            else {
                Toast.makeText(requireContext(), getString(R.string.profile_not_set_up), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val isRunning = app.sharedPref.getBoolean(getString(R.string.shr_pref_timer_running), false)
        if(!isRunning) return
        if(track.id.toString() != app.sharedPref.getString(getString(R.string.shr_pref_timer_track_id),null)) {
            activity?.stopService(timerServiceIntent)
            running = false
            timerTextView.text = Score.durationToString(Duration.ZERO)
            app.sharedPref.edit()
                .putBoolean(getString(R.string.shr_pref_timer_running), false)
                .remove(getString(R.string.shr_pref_timer_track_id))
                .remove(getString(R.string.shr_pref_timer_start))
                .apply()
            return
        }
        running = true
        // Register the broadcast receiver
        context?.registerReceiver(timerUpdateReceiver, IntentFilter(getString(R.string.TIMER_UPDATE_ACTION)))
        val startTime: String? = app.sharedPref.getString(getString(R.string.shr_pref_timer_start), null)
        if(startTime != null) {
            start = LocalDateTime.parse(startTime, formatter)
        }
    }

    override fun onPause() {
        super.onPause()
        if (running) {
            context?.unregisterReceiver(timerUpdateReceiver)
            app.sharedPref.edit()
                .putString(getString(R.string.shr_pref_timer_start), start.format(formatter))
                .putBoolean(getString(R.string.shr_pref_timer_running), running)
                .putString(getString(R.string.shr_pref_timer_track_id), track.id.toString())
                .apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isOnLocation(pt: Point): Boolean {
        if(app.location == null) return false
        val phoneLoc = Location("phoneLoc")
        phoneLoc.longitude = app.location!!.longitude
        phoneLoc.latitude = app.location!!.latitude

        val trackLoc = Location("trackLoc")
        trackLoc.longitude = pt.longitude
        trackLoc.latitude = pt.latitude

        Log.i(TAG, "phoneLoc: " + phoneLoc.toString())
        Log.i(TAG, "trackLoc: " + phoneLoc.toString())

        val distanceInMeters = phoneLoc.distanceTo(trackLoc)

        Log.i(TAG, "distance: " + distanceInMeters + " meters")
        return distanceInMeters <= 5
    }

    private fun trackStart() {
        if(!running && end == LocalDateTime.MIN) {
            if(!isOnLocation(track.start)) {
                Toast.makeText(requireContext(), "You're not on location", Toast.LENGTH_SHORT).show()
                return
            }
            start = LocalDateTime.now()
            running = true
            _binding!!.btnTimerReset.setBackgroundColor(requireContext().getColor(R.color.light_gray))
            _binding!!.btnTimerReset.isEnabled = false
            // Register the broadcast receiver
            context?.registerReceiver(timerUpdateReceiver, IntentFilter(getString(R.string.TIMER_UPDATE_ACTION)))
            activity?.startService(timerServiceIntent)
        }
    }

    private fun trackEnd() {
        if(running) {
            if(!isOnLocation(track.finish!!)) {
                Toast.makeText(requireContext(), "You're not on location", Toast.LENGTH_SHORT).show()
                return
            }
            end = LocalDateTime.now()
            running = false
            timerTextView.setTextColor(requireContext().getColor(R.color.emerald_700))
            _binding!!.btnTimerReset.setBackgroundColor(requireContext().getColor(R.color.gray))
            _binding!!.btnTimerReset.isEnabled = true
            _binding!!.btnSubmit.visibility = View.VISIBLE
            context?.unregisterReceiver(timerUpdateReceiver)
            activity?.stopService(timerServiceIntent)
        }
    }

    private fun submitScore(): Boolean {
        if(app.data.racer == null) return false
        val score = Score(app.data.racer!!.id,start,end)
        app.addTrackScore(track.id,score)
        return true
    }
}