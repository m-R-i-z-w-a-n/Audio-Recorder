package app.entertainment.accentzero.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import app.entertainment.accentzero.databinding.ItemScoreBinding
import app.entertainment.accentzero.databinding.ItemScoreDialogBinding

class ScoreAdapter(private val context: Context, private val scores: HashMap<String, Int>) :
    BaseAdapter() {

    override fun registerDataSetObserver(observer: DataSetObserver?) {}

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {}

    override fun getCount(): Int = scores.size

    override fun getItem(position: Int): Any = position

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false

    @SuppressLint("ViewHolder", "InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val judgeKey = position + 1
        var score = scores[judgeKey.toString()]
        val binding = ItemScoreBinding.inflate(LayoutInflater.from(context), null, false)

        binding.apply {
            if (judgeKey == 6) {
                judge.text = "Total"
                progressBar.max = 25
                score = scores.values.filter { it != scores["6"] }.sum()
                progressBar.progress = score!!
                tvScoreObtained.text = "${score}/${progressBar.max}"
            } else {
                progressBar.max = 5
                progressBar.progress = score!!
                judge.text = "${judge.text} $judgeKey"
                tvScoreObtained.text = score.toString()
            }

            tvAccent.text = setAccent(progressBar, score!!)
        }

        binding.root.setOnClickListener {
            if (judgeKey == 6)
                return@setOnClickListener

            showEditDialog(judgeKey, binding.tvScoreObtained)
        }

        return binding.root
    }

    fun getScores() = scores

    fun getTotalScore() = scores.values.filter { it != scores["6"] }.sum()

    private fun showEditDialog(position: Int, textView: TextView) {
        AlertDialog.Builder(context).apply {
            val binding = ItemScoreDialogBinding.inflate(LayoutInflater.from(context), null, false)
            setView(binding.root)

            setTitle("Enter scores")

            setPositiveButton("OK") { _, _ ->
                val newValue = binding.edtScore.text.toString()
                if (newValue.isEmpty() || newValue.toInt() > 5) {
                    Toast.makeText(context, "Enter a valid value!", Toast.LENGTH_SHORT).show()
                    showEditDialog(position, textView)
                    return@setPositiveButton
                }
                scores[position.toString()] = newValue.toInt()
                textView.text = newValue
                notifyDataSetChanged()
            }
            create()
        }.show()
    }

    private fun setAccent(progressBar: ProgressBar, score: Int): CharSequence {
        if (progressBar.max == 5)
            return when (score) {
                5 -> ACCENTS.FLUENT_ACCENT.value
                4 -> ACCENTS.MILD_ACCENT.value
                3 -> ACCENTS.MODERATE_ACCENT.value
                2 -> ACCENTS.WEAK_ACCENT.value
                1 -> ACCENTS.NO_ACCENT.value
                else -> ACCENTS.UNDEFINED.value
            }

        else if (progressBar.max == 25)
            return when(score) {
                in 1..5 -> ACCENTS.NO_ACCENT.value
                in 6..10 -> ACCENTS.WEAK_ACCENT.value
                in 11..15 -> ACCENTS.MODERATE_ACCENT.value
                in 16..20 -> ACCENTS.MILD_ACCENT.value
                in 21..25 -> ACCENTS.FLUENT_ACCENT.value
                else -> ACCENTS.UNDEFINED.value
            }

        return ACCENTS.UNDEFINED.value
    }

    override fun getItemViewType(position: Int): Int = position

    override fun getViewTypeCount(): Int = if (count > 0) count else 1

    override fun isEmpty(): Boolean = false

    override fun areAllItemsEnabled(): Boolean = false

    override fun isEnabled(position: Int): Boolean = true

    private enum class ACCENTS(val value: String) {
        NO_ACCENT("no accent"),
        WEAK_ACCENT("weak accent"),
        MODERATE_ACCENT("moderate accent"),
        MILD_ACCENT("mild accent"),
        FLUENT_ACCENT("fluent accent"),
        UNDEFINED("undefined")
    }
}
