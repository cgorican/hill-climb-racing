package si.um.feri.hillclimbracing

import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.awaitAll
import si.um.feri.hillclimbracing.databinding.FragmentMainBinding
import si.um.feri.hillclimbracing.databinding.FragmentMapsBinding

class MapsFragment : Fragment(), LocationListener {
    private val TAG = MapsFragment::class.qualifiedName
    private var _binding: FragmentMapsBinding? = null
    private lateinit var app: HCRApplication

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        googleMap.clear()
        val f103 = LatLng(46.559102, 15.639008)
        for (track in app.data.tracks) {
            val tmpStartLoc = LatLng(track.start.latitude, track.start.longitude)
            val tmpFinishLoc = LatLng(track.finish.latitude, track.finish.longitude)
            googleMap.addMarker(
                MarkerOptions().position(tmpStartLoc).title("${track.title} [START]")
            )
            googleMap.addMarker(
                MarkerOptions().position(tmpFinishLoc).title("${track.title} [FINISH]")
            )
        }
        //googleMap.addMarker(MarkerOptions().position(f103).title("FERI F103"))
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(f103))
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(f103, 14f))
        if(activity is MainActivity) {
            googleMap.isMyLocationEnabled = (activity as MainActivity).checkLocationPermission()
        }
        if(app.location != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pointToLatLng(app.location!!), 14f))
        }
        else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(f103, 14f))
        }
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.google_map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Google maps style parsing failed.");
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Google maps style resource not found.")
        }
    }

    @SuppressLint("MissingPermission")
    private val updateCallback = OnMapReadyCallback { googleMap ->
        Log.i("LOC_UPDATE?", "now?")
        if(activity is MainActivity) {
            googleMap.isMyLocationEnabled = (activity as MainActivity).checkLocationPermission()
        }
        if(app.location != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pointToLatLng(app.location!!), 14f))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return inflater.inflate(R.layout.fragment_maps, container, false)
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = requireContext().applicationContext as HCRApplication
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapsFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun pointToLatLng(point: Point): LatLng = LatLng(point.latitude, point.longitude)

    private fun updateMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapsFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(updateCallback)
    }

    override fun onLocationChanged(location: Location) {
        Log.d("LOC_UPDATE", location.toString())
        updateMap()
    }
}