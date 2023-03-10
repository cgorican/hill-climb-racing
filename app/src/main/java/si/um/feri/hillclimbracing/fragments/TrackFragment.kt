package si.um.feri.hillclimbracing.fragments

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
import si.um.feri.hillclimbracing.*
import si.um.feri.hillclimbracing.activities.MainActivity
import si.um.feri.hillclimbracing.databinding.FragmentTrackBinding
import si.um.feri.hillclimbracing.enums.DifficultyEnum
import si.um.feri.hillclimbracing.services.TimerService
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


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
        timerUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Get the time remaining from the intent
                var timerValue = intent?.getLongExtra(getString(R.string.TIMER_VALUE), 0) ?: 0
                if (timerValue < 0) {
                    timerValue = 0
                    timerTextView.text = Score.durationToString(Duration.ZERO)
                } else {
                    timerTextView.text = Score.durationToString(Duration.ofMillis(timerValue))
                }
            }
        }

        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireContext().applicationContext as HCRApplication

        if (app.data.racer == null) {
            app.recoverUserBySharedPrefs()
        }

        track = app.data.tracks[args.index]

        // temp
        track.leaderboard.forEach({
            Log.i(TAG, it.racerId+"")
        })

        // difficulty bubble
        _binding!!.displayDifficulty.text = track.difficulty.value.toString()
        when (track.difficulty.value) {
            DifficultyEnum.HARD.value -> _binding!!.displayDifficulty.background.setTint(
                requireContext().getColor(R.color.diff_hard)
            )
            DifficultyEnum.MEDIUM.value -> _binding!!.displayDifficulty.background.setTint(
                requireContext().getColor(R.color.diff_medium)
            )
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
            if (submitScore()) {
                Navigation.findNavController(view)
                    .navigate(R.id.action_trackFragment_to_mainFragment)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.profile_not_set_up),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val isRunning = app.sharedPref.getBoolean(getString(R.string.SHR_PREF_TIMER_RUNNING), false)
        if (!isRunning) return
        if (track.id.toString() != app.sharedPref.getString(
                getString(R.string.SHR_PREF_TIMER_TRACK_ID),
                null
            )
        ) {
            activity?.stopService(timerServiceIntent)
            running = false
            timerTextView.text = Score.durationToString(Duration.ZERO)
            app.sharedPref.edit()
                .putBoolean(getString(R.string.SHR_PREF_TIMER_RUNNING), false)
                .remove(getString(R.string.SHR_PREF_TIMER_TRACK_ID))
                .remove(getString(R.string.SHR_PREF_TIMER_START))
                .apply()
            return
        }
        running = true
        // Register the broadcast receiver
        context?.registerReceiver(
            timerUpdateReceiver,
            IntentFilter(getString(R.string.TIMER_UPDATE_ACTION))
        )
        val startTime: String? =
            app.sharedPref.getString(getString(R.string.SHR_PREF_TIMER_START), null)
        if (startTime != null) {
            start = LocalDateTime.parse(startTime, formatter)
        }
    }

    override fun onPause() {
        super.onPause()
        if (running) {
            context?.unregisterReceiver(timerUpdateReceiver)
            app.sharedPref.edit()
                .putString(getString(R.string.SHR_PREF_TIMER_START), start.format(formatter))
                .putBoolean(getString(R.string.SHR_PREF_TIMER_RUNNING), running)
                .putString(getString(R.string.SHR_PREF_TIMER_TRACK_ID), track.id.toString())
                .apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getLocationDistance(pt: Point): Float {
        if (app.location == null) return Float.MAX_VALUE
        val phoneLoc = Location("phoneLoc")
        phoneLoc.longitude = app.location!!.longitude
        phoneLoc.latitude = app.location!!.latitude

        val trackLoc = Location("trackLoc")
        trackLoc.longitude = pt.longitude
        trackLoc.latitude = pt.latitude

        var distanceInMeters = phoneLoc.distanceTo(trackLoc)

        if (distanceInMeters <= 5) {
            distanceInMeters = 0f
        }

        return distanceInMeters
    }

    private fun trackStart() {
        if (app.data.racer == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.profile_not_set_up),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!(activity as MainActivity).checkLocationPermission()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.location_disabled),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!running && end == LocalDateTime.MIN) {
            val distanceToStartPoint = getLocationDistance(track.start)
            if (distanceToStartPoint > 0) {
                Toast.makeText(
                    requireContext(), String.format(
                        getString(R.string.loc_distance_meters_away),
                        distanceToStartPoint.roundToInt()
                    ), Toast.LENGTH_SHORT
                ).show()
                return
            }
            start = LocalDateTime.now()
            running = true
            _binding!!.btnTimerReset.setBackgroundColor(requireContext().getColor(R.color.light_gray))
            _binding!!.btnTimerReset.isEnabled = false
            // Register the broadcast receiver
            context?.registerReceiver(
                timerUpdateReceiver,
                IntentFilter(getString(R.string.TIMER_UPDATE_ACTION))
            )
            activity?.startService(timerServiceIntent)
        }
    }

    private fun trackEnd() {
        if (running) {
            val distanceToFinishPoint = getLocationDistance(track.finish)
            if (distanceToFinishPoint > 0) {
                Toast.makeText(
                    requireContext(), String.format(
                        getString(R.string.loc_distance_meters_to_finish),
                        distanceToFinishPoint.roundToInt()
                    ), Toast.LENGTH_SHORT
                ).show()
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
        if (app.data.racer == null) return false
        val score = Score(app.data.racer!!.id, Score.toFormattedString(start), Score.toFormattedString(end))
        app.addTrackScore(track.id, score)
        return true
    }
}