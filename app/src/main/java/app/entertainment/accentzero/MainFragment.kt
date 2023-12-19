package app.entertainment.accentzero

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import app.entertainment.accentzero.databinding.FragmentMainBinding
import app.entertainment.accentzero.models.Word

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment#newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment(R.layout.fragment_main) {
    private var _binding: FragmentMainBinding? = null

    // Use binding delegate for view binding to prevent memory leaks
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize view binding
        _binding = FragmentMainBinding.bind(view)

        // Set up click listeners for word buttons to open PronunciationFragment
        openPronunciationPage()
    }

    /**
     * Iterates through each word button in the layout and sets up click listeners
     * to open PronunciationFragment with details about the selected word.
     */
    private fun openPronunciationPage() {
        val relativeLayout = binding.buttonGroup

        for (i in 0 until relativeLayout.childCount) {
            val view: View = relativeLayout.getChildAt(i)
            // Skip the view if it's is not a button
            if (view !is Button)
                continue

            // Create a Word object with details from the button
            val word = Word(view.text.toString().uppercase(), view.tag.toString())

            // Bundle the Word object and pass it to PronunciationFragment
            val bundle = Bundle().also {
                it.putSerializable("word", word)
            }

            view.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(
                        R.id.fragment_container,
                        PronunciationFragment().also { it.arguments = bundle }
                    )
                    addToBackStack(null)
                    commit()
                }
            }
        }
    }

    override fun onDestroy() {
        // Release the view binding to avoid memory leaks
        _binding = null
        super.onDestroy()
    }
}