package app.entertainment.accentzero.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import app.entertainment.accentzero.databinding.ItemRecentClipsBinding

class RecentClipsAdapter(private val context: Context, private val clips: List<String>) :
    BaseAdapter() {
    override fun getCount(): Int = clips.size

    override fun getItem(position: Int): Any = position

    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding = ItemRecentClipsBinding.inflate(LayoutInflater.from(context), null, false)

        binding.obtainedScores.text = "18/25"

        return binding.root
    }
}