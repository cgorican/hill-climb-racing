package si.um.feri.hillclimbracing

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
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
    lateinit var requestLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Global state
        app = application as HCRApplication
        app.mainActivity = this

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

        // location
        checkLocationPermissions()

        // notifications
        checkNotificationsPermissions()
        Log.i(TAG,"Notification ${app.notificationsEnabled}")
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
    private fun checkLocationPermissions() {
        requestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted) {
                locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10,
                    3f,
                    this
                )
            }
            else {
                Toast.makeText(this,"Location is disabled!",Toast.LENGTH_SHORT).show()
            }
        }
        requestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun checkNotificationsPermissions() {
        val notificationsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            app.notificationsEnabled = isGranted
            if(!isGranted) {
                Toast.makeText(this,"Notifications are disabled!",Toast.LENGTH_SHORT).show()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else mutableSetOf(true)
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

    fun displayNotification(track: Track) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(!app.notificationsEnabled) return
        val notification = NotificationCompat.Builder(applicationContext, getString(R.string.NOTIFICATIONS_CHANNEL_ID))
            .setContentTitle(getString(R.string.NOTIFICATION_NEW_TRACK))
            .setContentText("${track.title} ${track.description}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        notificationManager.notify(1,notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        app.mainActivity = null
    }
}