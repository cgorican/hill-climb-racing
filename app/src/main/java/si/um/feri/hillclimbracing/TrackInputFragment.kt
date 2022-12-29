package si.um.feri.hillclimbracing

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import si.um.feri.hillclimbracing.databinding.FragmentTrackInputBinding
import si.um.feri.hillclimbracing.enums.DifficultyEnum

class TrackInputFragment : Fragment() {
    private val TAG = TrackInputFragment::class.qualifiedName
    private lateinit var app: HCRApplication
    private val args: TrackInputFragmentArgs by navArgs()
    private var _binding: FragmentTrackInputBinding? = null

    lateinit var track: Track

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_track_input, container, false)
        _binding = FragmentTrackInputBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireContext().applicationContext as HCRApplication

        setupDifficultySpinner()

        if (args.index != -1) {
            track = app.data.tracks[args.index]
            _binding!!.btnSetTrack.text = getString(R.string.btn_edit)
            _binding!!.inputTrackTitle.setText(track.title)
            _binding!!.inputTrackDesc.setText(track.description)

            _binding!!.spinnerDifficulty.setSelection(track.difficulty.value-1)

            _binding!!.inputStartLoc.setText(
                String.format(
                    "%f/%f",
                    track.start.latitude,
                    track.start.longitude
                )
            )
            _binding!!.inputFinishLoc.setText(
                String.format(
                    "%f/%f",
                    track.start.latitude,
                    track.start.longitude
                )
            )
        } else {
            _binding!!.btnDelete.visibility = View.INVISIBLE
        }

        _binding!!.btnSetTrack.setOnClickListener {
            if (submitTrack()) {
                Navigation.findNavController(view)
                    .navigate(R.id.action_trackInputFragment_to_mainFragment)
            }
        }

        _binding!!.btnDelete.setOnClickListener {
            deleteTrackModal(_binding!!.btnDelete)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupDifficultySpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.array_track_difficulty,
            android.R.layout.simple_spinner_item
        )
        _binding!!.spinnerDifficulty.adapter = adapter
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    private fun submitTrack(): Boolean {
        val title = _binding!!.inputTrackTitle.text.toString().trim()
        val desc = _binding!!.inputTrackDesc.text.toString().trim()
        val startString = _binding!!.inputStartLoc.text.toString().trim()
        val finishString = _binding!!.inputStartLoc.text.toString().trim()
        val diff: DifficultyEnum
        if (title.isEmpty() || startString.isEmpty() || finishString.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.empty_fields),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        val start: Point
        val finish: Point

        try {
            val startSplit = startString.split('/')
            if (startSplit.size == 2) {
                start = Point(startSplit[0].toDouble(), startSplit[1].toDouble())
            } else throw Exception(getString(R.string.invalid_location_start))
        } catch (e: Exception) {
            Log.e(TAG, "Invalid start location")
            Toast.makeText(
                requireContext(),
                getString(R.string.invalid_location_start),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        try {
            val finishSplit = finishString.split('/')
            if (finishSplit.size == 2) {
                finish = Point(finishSplit[0].toDouble(), finishSplit[1].toDouble())
            } else throw Exception(getString(R.string.invalid_location_finish))
        } catch (e: Exception) {
            Log.e(TAG, "Invalid finish location")
            Toast.makeText(
                requireContext(),
                getString(R.string.invalid_location_finish),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        val diffItemPos = _binding!!.spinnerDifficulty.selectedItemPosition
        diff = when (diffItemPos + 1) {
            DifficultyEnum.EASY.value -> DifficultyEnum.EASY
            DifficultyEnum.MEDIUM.value -> DifficultyEnum.MEDIUM
            DifficultyEnum.HARD.value -> DifficultyEnum.HARD
            else -> {
                Log.i(TAG, "Difficulty: $diffItemPos")
                return false
            }
        }

        if (this::track.isInitialized) {
            track.title = title
            track.description = desc
            track.difficulty = diff
            track.start = start
            track.finish = finish
            app.addTrack(track)
        } else if (desc.isEmpty()) {
            track = Track(title, diff, start, finish)
            app.putTrack(track)
        } else {
            track = Track(title, diff, start, finish, desc)
            app.putTrack(track)
        }
        return true
    }

    private fun deleteTrackModal(view: View) {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(getString(R.string.modal_delete_track_title))
        builder.setMessage(String.format(getString(R.string.modal_delete_track_desc),track.title))
        builder.setPositiveButton(getString(R.string.btn_delete)) { dialogInterface, it ->
            app.deleteTrack(track.id)
            Navigation.findNavController(view)
                .navigate(R.id.action_trackInputFragment_to_mainFragment)
        }
        .setNegativeButton(getString(R.string.btn_cancel)) { dialogInterface, _ ->
            dialogInterface.cancel()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()
    }
}