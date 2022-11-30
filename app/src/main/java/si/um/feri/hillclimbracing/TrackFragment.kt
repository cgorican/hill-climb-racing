package si.um.feri.hillclimbracing

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import si.um.feri.hillclimbracing.databinding.FragmentTrackBinding
import si.um.feri.hillclimbracing.enums.DifficultyEnum
import java.time.Duration
import java.time.LocalDateTime


class TrackFragment : Fragment() {
    private lateinit var app: HCRApplication
    private lateinit var start: LocalDateTime
    private lateinit var track: Track

    private var _binding: FragmentTrackBinding? = null
    private val args: TrackInputFragmentArgs by navArgs()
    private var end: LocalDateTime = LocalDateTime.MIN
    private var running = false
    //private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss SSS")

    private lateinit var timerTextView: TextView

    companion object {
        val msUntilAutoStop: Long = 8 * 60 * 60 * 1000
    }

    val cdTimer = object : CountDownTimer(msUntilAutoStop, 1) {
        override fun onTick(millisUntilFinished: Long) {
            val duration = Duration.between(start, LocalDateTime.now())
            timerTextView.text = Score.durationToString(duration)
        }

        override fun onFinish() {
            cancel()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTrackBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireContext().applicationContext as HCRApplication

        if (args.index == -1) {
            Navigation.findNavController(view)
                .navigate(R.id.action_trackFragment_to_mainFragment)
        }
        track = app.data.tracks[args.index]

        _binding!!.displayDifficulty.text = track.difficulty.value.toString()
        when (track.difficulty.value) {
            DifficultyEnum.HARD.value -> _binding!!.displayDifficulty.background.setTint(requireContext().getColor(R.color.diff_hard))
            DifficultyEnum.MEDIUM.value -> _binding!!.displayDifficulty.background.setTint(requireContext().getColor(R.color.diff_medium))
            else -> _binding!!.displayDifficulty.background.setTint(requireContext().getColor(R.color.diff_easy))
        }


        /*
        running = app.sharedPref.getBoolean(getString(R.string.shr_pref_timer_running), false)
        if(running) {
            val startString = app.sharedPref.getString(getString(R.string.shr_pref_timer_start),null)
            if(startString != null) {
                start = LocalDateTime.parse(startString, formatter)
            }
        }
        */

        timerTextView = _binding!!.timer
        timerTextView.text = Score.durationToString(Duration.ZERO)
        timerTextView.setTextColor(requireContext().getColor(R.color.gray))
        _binding!!.btnTimerReset.setBackgroundColor(requireContext().getColor(R.color.light_gray))
        _binding!!.btnSubmit.visibility = View.INVISIBLE

        _binding!!.timer.setOnClickListener {
            if (running) {   // STOP
                end = LocalDateTime.now()
                running = false
                timerTextView.setTextColor(requireContext().getColor(R.color.emerald_700))
                _binding!!.btnTimerReset.setBackgroundColor(requireContext().getColor(R.color.gray))
                _binding!!.btnTimerReset.isEnabled = true
                _binding!!.btnSubmit.visibility = View.VISIBLE
                stopTimer()
            } else if (end == LocalDateTime.MIN) { // START
                start = LocalDateTime.now()
                running = true
                _binding!!.btnTimerReset.setBackgroundColor(requireContext().getColor(R.color.light_gray))
                _binding!!.btnTimerReset.isEnabled = false
                startTimer()
            }
        }

        _binding!!.btnTimerReset.setOnClickListener {
            if (!running && end.isAfter(start)) {
                end = LocalDateTime.MIN
                timerTextView.text = Score.durationToString(Duration.ZERO)
                timerTextView.setTextColor(requireContext().getColor(R.color.gray))
                _binding!!.btnTimerReset.setBackgroundColor(requireContext().getColor(R.color.light_gray))
                _binding!!.btnSubmit.visibility = View.INVISIBLE
                _binding!!.btnTimerReset.isEnabled = true
            }
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        /*
        if (running) {
            app.sharedPref.edit()
                .putString(getString(R.string.shr_pref_timer_start), start.format(formatter))
                .putBoolean(getString(R.string.shr_pref_timer_running), running)
                .apply()
        }
         */
    }

    private fun startTimer() {
        cdTimer.start()
    }

    private fun stopTimer() {
        cdTimer.cancel()
    }

    private fun submitScore(): Boolean {
        if(app.data.racer == null) return false
        val score = Score(app.data.racer!!.id,start,end)
        app.data.setTrackScore(track.id,score)
        return true
    }
}