package si.um.feri.hillclimbracing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import si.um.feri.hillclimbracing.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var app: HCRApplication
    val mainFragment = MainFragment()
    val mapsFragment = MapsFragment()
    val racerFragment = RacerFragment()
    val trackFragment = TrackFragment()
    val trackInputFragment = TrackInputFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app = application as HCRApplication

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // use navGraph
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragmentContainerView, mainFragment)
            .commit()
    }
}