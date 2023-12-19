package app.entertainment.accentzero.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import app.entertainment.accentzero.R
import app.entertainment.accentzero.databinding.ItemRecordingBinding
import app.entertainment.accentzero.player.AudioPlayer
import java.io.File

class RecordingAdapter(private val context: Context, private val recordingList: List<String>):
    BaseAdapter() {
    override fun registerDataSetObserver(observer: DataSetObserver?) { }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) { }

    override fun getCount(): Int = recordingList.size

    override fun getItem(position: Int): Any = position

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false

    @SuppressLint("MissingInflatedId", "ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val filePath = recordingList[position]
        val binding = ItemRecordingBinding.inflate(LayoutInflater.from(context), null, false)

        binding.playClip.setOnClickListener {
            val mediaPlayer = AudioPlayer(context)
            mediaPlayer.play(File(filePath))
        }

        return binding.root
    }

    fun getRecordings() = recordingList

    override fun getItemViewType(position: Int): Int = position

    override fun getViewTypeCount(): Int = if (count > 0) count else 1

    override fun isEmpty(): Boolean = false

    override fun areAllItemsEnabled(): Boolean = false

    override fun isEnabled(position: Int): Boolean = true

}