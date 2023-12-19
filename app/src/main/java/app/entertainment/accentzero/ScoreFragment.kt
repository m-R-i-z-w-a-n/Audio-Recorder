package app.entertainment.accentzero

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import app.entertainment.accentzero.adapters.ScoreAdapter
import app.entertainment.accentzero.databinding.FragmentScoreBinding
import app.entertainment.accentzero.player.AudioPlayer
import app.entertainment.accentzero.utils.Constants.JUDGE_1
import app.entertainment.accentzero.utils.Constants.JUDGE_2
import app.entertainment.accentzero.utils.Constants.JUDGE_3
import app.entertainment.accentzero.utils.Constants.JUDGE_4
import app.entertainment.accentzero.utils.Constants.JUDGE_5
import java.io.File

/**
 * A simple [Fragment] subclass.
 * Use the [ScoreFragment#newInstance] factory method to
 * create an instance of this fragment.
 */
class ScoreFragment : Fragment(R.layout.fragment_score) {
    private var _fragmentBinding: FragmentScoreBinding? = null

    private val fragmentBinding get() = _fragmentBinding!!

    private lateinit var scores: HashMap<String, Int>

    private lateinit var word: String

    private var filePath: String? = null

    private lateinit var scoreAdapter: ScoreAdapter

    private val SHARED_PREFERENCE_NAME by lazy { filePath?.let { File(it).nameWithoutExtension } ?: word }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _fragmentBinding = FragmentScoreBinding.bind(view)

        word = arguments?.getString("word")!!

        fragmentBinding.word.text = "\"${word}\""

        filePath = (arguments?.getString("file_path") ?: Toast.makeText(requireContext(), "filePath is null!", Toast.LENGTH_LONG).show()).toString()

        initListView()

        fragmentBinding.playClip.setOnClickListener {
            val mediaPlayer = AudioPlayer(requireContext())
            mediaPlayer.play(File(filePath ?: return@setOnClickListener))
        }
    }

    private fun initListView() {
        fragmentBinding.scoreListView.apply {
            scores = hashMapOf()
            scores["1"] = 5
            scores["2"] = 4
            scores["3"] = 3
            scores["4"] = 2
            scores["5"] = 1
            scores["6"] = 25

            // Hide the divider
            divider = ColorDrawable(resources.getColor(android.R.color.transparent, resources.newTheme()))
            dividerHeight = 0

            scoreAdapter = ScoreAdapter(requireContext(), scores)
            adapter = scoreAdapter
        }
    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().apply{
            val myScores = scoreAdapter.getScores()

            putInt(JUDGE_1, myScores["1"]!!)
            putInt(JUDGE_2, myScores["2"]!!)
            putInt(JUDGE_3, myScores["3"]!!)
            putInt(JUDGE_4, myScores["4"]!!)
            putInt(JUDGE_5, myScores["5"]!!)
            apply()
        }
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        scores["1"] = sharedPreferences.getInt(JUDGE_1, 0)
        scores["2"] = sharedPreferences.getInt(JUDGE_2, 0)
        scores["3"] = sharedPreferences.getInt(JUDGE_3, 0)
        scores["4"] = sharedPreferences.getInt(JUDGE_4, 0)
        scores["5"] = sharedPreferences.getInt(JUDGE_5, 0)

        // Update the adapter with the new scores
        scoreAdapter = ScoreAdapter(requireContext(), scores)
    }

    override fun onDestroy() {
        _fragmentBinding = null
        super.onDestroy()
    }
}