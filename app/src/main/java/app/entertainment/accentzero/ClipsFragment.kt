package app.entertainment.accentzero

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.entertainment.accentzero.adapters.ClipsAdapter
import app.entertainment.accentzero.db.DatabaseHelper
import app.entertainment.accentzero.databinding.FragmentClipsBinding
import java.io.File

/**
 * A simple [Fragment] subclass.
 * Use the [ClipsFragment#newInstance] factory method to
 * create an instance of this fragment.
 */
class ClipsFragment : Fragment(R.layout.fragment_clips) {
    private var _binding: FragmentClipsBinding? = null

    private val binding get() = _binding!!

    private val databaseHelper by lazy { DatabaseHelper(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentClipsBinding.bind(view)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.rvClips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = ClipsAdapter(
                requireContext(),
                databaseHelper.getAllAudioFiles().onEach { filePath ->
                    if (!File(filePath).exists()) {
                        databaseHelper.deleteRecord(filePath)
                        return@onEach
                    }
                }.sorted()
            )
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}