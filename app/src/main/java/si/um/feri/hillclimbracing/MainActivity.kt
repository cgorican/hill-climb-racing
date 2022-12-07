package si.um.feri.hillclimbracing

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import si.um.feri.hillclimbracing.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), LocationListener {
    private val TAG = MainActivity::class.qualifiedName
    private lateinit var binding: ActivityMainBinding
    private lateinit var app: HCRApplication
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Global state
        app = application as HCRApplication

        // Toolbar
        val navHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.mapsFragment, R.id.mainFragment, R.id.racerFragment)
        )

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Bottom navigation
        binding.bottomNav.setupWithNavController(navController)

        initLocationService()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun checkLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun initLocationService() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        if (checkLocationPermission()) {
            // ask permissions here using below code
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10,
                3f,
                this
            )
        }
    }



    override fun onLocationChanged(location: Location) {
        app.location = Point(location.latitude, location.longitude)
        Log.i("LOC", location.toString())
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && checkLocationPermission()) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10,
                3f,
                this
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun subscribeOnLocationUpdates(listener: LocationListener) {
        if(checkLocationPermission()) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10,
                3f,
                listener
            )
        }
    }
}