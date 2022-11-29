package si.um.feri.hillclimbracing

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import si.um.feri.hillclimbracing.databinding.FragmentRacerBinding

class RacerFragment : Fragment() {
    private var _binding: FragmentRacerBinding? = null

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

        _binding?.btnSetRacer!!.setOnClickListener {
            Log.i(RacerFragment::class.qualifiedName, "Confirm profile setup")
            Navigation.findNavController(view).navigate(R.id.action_racerFragment_to_mainFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupProfile() {
        val firstname = _binding!!.inputFirstname.text
        val lastName = _binding!!.inputLastname.text
        val email = _binding!!.inputEmail.text

        val birthdate = _binding!!.inputBirthdate
    }
}