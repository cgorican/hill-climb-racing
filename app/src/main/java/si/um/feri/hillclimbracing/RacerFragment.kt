package si.um.feri.hillclimbracing

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import si.um.feri.hillclimbracing.databinding.FragmentRacerBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RacerFragment : Fragment() {
    private val TAG = RacerFragment::class.qualifiedName
    private var _binding: FragmentRacerBinding? = null
    lateinit var app: HCRApplication

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_racer, container, false)
        _binding = FragmentRacerBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireContext().applicationContext as HCRApplication

        if (app.data.racer != null) {
            _binding?.inputFirstname!!.setText(app.data.racer!!.firstname)
            _binding?.inputLastname!!.setText(app.data.racer!!.lastname)
            _binding?.inputEmail!!.setText(app.data.racer!!.email)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            _binding?.inputBirthdate!!.setText(app.data.racer!!.birthdate.format(formatter))
            _binding?.inputFirstname!!.isEnabled = false
            _binding?.inputLastname!!.isEnabled = false
            _binding?.inputBirthdate!!.isEnabled = false
        }

        _binding?.btnSetRacer!!.setOnClickListener {
            if (submitProfile()) {
                Log.i(TAG, "Profile updated")
                Navigation.findNavController(view)
                    .navigate(R.id.action_racerFragment_to_mainFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun submitProfile(): Boolean {
        val firstname = _binding!!.inputFirstname.text.toString().trim()
        val lastname = _binding!!.inputLastname.text.toString().trim()
        val email = _binding!!.inputEmail.text.toString().trim()
        val birthdateString = _binding!!.inputBirthdate.text.toString().trim()

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val birthdate: LocalDate

        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || birthdateString.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.empty_fields), Toast.LENGTH_SHORT)
                .show()
            return false
        }

        try {
            birthdate = LocalDate.parse(birthdateString, formatter)
            if (birthdate > LocalDate.now()) throw Exception(getString(R.string.invalid_birthdate))
        } catch (e: Exception) {
            Log.e(TAG, "Invalid birthdate: $birthdateString")
            Toast.makeText(
                requireContext(),
                getString(R.string.invalid_birthdate),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (app.data.racer == null) {
            val racer = Racer(firstname, lastname, email, birthdate)
            app.data.racer = racer
            app.sharedPref
                .edit()
                .putString(getString(R.string.shr_pref_racer_id), racer.id.toString())
                .apply()
            app.saveRacer()
        } else {
            app.data.racer!!.email = email
            app.updateRacer()
        }
        return true
    }
}