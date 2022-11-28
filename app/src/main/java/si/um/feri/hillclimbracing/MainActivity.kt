package si.um.feri.hillclimbracing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import si.um.feri.hillclimbracing.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val googleMaps = MapsFragment()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragmentContainerView, googleMaps)
            //.add(R.id.fragmentContainerView, inputFragment)
            .commit()
    }
}