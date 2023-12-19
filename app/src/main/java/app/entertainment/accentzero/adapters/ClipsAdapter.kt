package app.entertainment.accentzero.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.entertainment.accentzero.databinding.ItemClipsBinding
import app.entertainment.accentzero.player.AudioPlayer
import java.io.File

class ClipsAdapter(private val context: Context, private val files: List<String>): RecyclerView.Adapter<ClipsAdapter.ClipsViewHolder>() {
    inner class ClipsViewHolder(val binding: ItemClipsBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipsViewHolder {
        return ClipsViewHolder(ItemClipsBinding.inflate(LayoutInflater.from(context), null, false))
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: ClipsViewHolder, position: Int) {
        val audioFile = File(files[position])
        val word = audioFile.nameWithoutExtension.lowercase().replaceFirstChar(Char::uppercase)

        holder.binding.apply {
            clipTitle.text = word
            playClip.setOnClickListener {
                AudioPlayer(context).also { it.play(audioFile) }
            }
        }
    }
}