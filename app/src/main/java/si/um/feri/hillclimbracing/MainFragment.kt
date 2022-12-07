package si.um.feri.hillclimbracing

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import si.um.feri.hillclimbracing.adapters.TrackAdapter
import si.um.feri.hillclimbracing.databinding.FragmentMainBinding
import si.um.feri.hillclimbracing.databinding.FragmentRacerBinding

class MainFragment : Fragment(), TrackAdapter.OnItemClickListener {
    private val TAG = MainFragment::class.qualifiedName
    private var _binding: FragmentMainBinding? = null

    private lateinit var app: HCRApplication
    private lateinit var adapter: TrackAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_main, container, false)
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireContext().applicationContext as HCRApplication

        val layoutManager = LinearLayoutManager(context)
        recyclerView = _binding!!.recyclerTracks
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        adapter = TrackAdapter(app.data.tracks, requireContext(), this)
        recyclerView.adapter = adapter

        _binding!!.floatingActionButton.setOnClickListener {
            Log.i(TAG, "Navigate to add a new track fragment")
            val title = getString(R.string.title_add_track)
            val action = MainFragmentDirections.actionMainFragmentToTrackInputFragment(title=title)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(p0: View?, position: Int) {
        Log.i(TAG, "Track[${position}] clicked (${app.data.tracks[position].title})")
        val title: String = app.data.tracks[position].title
        val action = MainFragmentDirections.actionMainFragmentToTrackFragment(title=title, index=position)
        findNavController().navigate(action)
    }

    override fun onItemLongClick(p0: View?, position: Int) {
        Log.i(TAG, "Track[${position}] long click (${app.data.tracks[position].title})")
        val title = getString(R.string.title_edit_track)
        val action = MainFragmentDirections.actionMainFragmentToTrackInputFragment(title=title, index=position)
        findNavController().navigate(action)
    }
}