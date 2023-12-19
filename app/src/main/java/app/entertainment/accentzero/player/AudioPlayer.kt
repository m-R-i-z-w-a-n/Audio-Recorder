package app.entertainment.accentzero.player

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import androidx.core.net.toUri
import java.io.File

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    var completionListener: OnCompletionListener? = null

    fun play(file: File) {
        MediaPlayer.create(context, file.toUri()).apply {
            start()
            setOnCompletionListener {
                completionListener?.onCompletion(it)
            }
            mediaPlayer = this
        }
    }

    fun isPlaying() = mediaPlayer?.isPlaying ?: false

    fun getAudioSessionId() = mediaPlayer?.audioSessionId

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}