package app.entertainment.accentzero

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import app.entertainment.accentzero.adapters.RecentClipsAdapter
import app.entertainment.accentzero.adapters.RecordingAdapter
import app.entertainment.accentzero.db.DatabaseHelper
import app.entertainment.accentzero.databinding.FragmentPronunciationBinding
import app.entertainment.accentzero.databinding.RecordAudioDialogueBinding
import app.entertainment.accentzero.models.Word
import app.entertainment.accentzero.player.AudioPlayer
import app.entertainment.accentzero.recorder.AudioRecorder
import com.visualizer.amplitude.AudioRecordView
import java.io.File
import java.util.Timer
import java.util.TimerTask


/**
 * A simple [Fragment] subclass.
 * Use the [PronunciationFragment#newInstance] factory method to
 * create an instance of this fragment.
 */
class PronunciationFragment : Fragment(R.layout.fragment_pronunciation) {
    private var _fragmentBinding: FragmentPronunciationBinding? = null
    private var _dialogBinding: RecordAudioDialogueBinding? = null

    private val fragmentBinding get() = _fragmentBinding!!
    private val dialogBinding get() = _dialogBinding!!

    private lateinit var targetWord: String

    private var audioFile: File? = null
    private var filePath: String? = null

    private var isRecording = false

    private var timer: Timer? = null

    private var dialog: Dialog? = null

    private lateinit var recordingAdapter: RecordingAdapter
    private val databaseHelper by lazy { DatabaseHelper(requireContext()) }

    private lateinit var recentClipsAdapter: RecentClipsAdapter

    private val recorder by lazy { AudioRecorder(requireContext()) }
    private val player by lazy { AudioPlayer(requireContext()) }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _fragmentBinding = FragmentPronunciationBinding.bind(view)

        val word = arguments?.getSerializable("word") as Word
        targetWord = word.word

        fragmentBinding.tvHowToPronounce.text =
            "${fragmentBinding.tvHowToPronounce.text}\"${targetWord}\""

        fragmentBinding.tvPartOfSpeech.text = word.typeOfSpeech

        initListView()

        // Set up click listener for the practice button to show the recording dialog
        fragmentBinding.btnPractice.setOnClickListener {
            dialog?.show() ?: initDialog().also { dialog!!.show() }
        }
    }

    /**
     * Initialize the list views for recordings
     */
    private fun initListView() {
        val recordings = hashSetOf<String>()

        // Iterate through all audio files in the database
        databaseHelper.getAllAudioFiles().forEach {
            // Check if file exists in storage
            if (!File(it).exists()) {
                // If not, delete the record from the database
                databaseHelper.deleteRecord(it)
                return@forEach
            }

            // Filter recordings related to the target word
            if (it.endsWith("${targetWord}.mp3") || it.contains("${targetWord}_"))
                recordings.add(it)
        }

        // Initialize recording adapter for the main list view
        recordingAdapter = RecordingAdapter(requireContext(), recordings.toList())
        fragmentBinding.clipsListView.apply {
            adapter = recordingAdapter
            setOnItemClickListener { _, _, position, _ ->
                // Handle item click to navigate to the score fragment
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    val savedRecordings = recordingAdapter.getRecordings()

                    val bundle = Bundle().also { it.putString("word", targetWord) }
                        .also { it.putString("file_path", savedRecordings[position]) }

                    replace(R.id.fragment_container, ScoreFragment().also { it.arguments = bundle })
                    addToBackStack(null)
                    commit()
                }
            }
        }

        // Initialize recent clips adapter for the secondary list view
        fragmentBinding.recentClipsListView.apply {
            recentClipsAdapter = RecentClipsAdapter(requireContext(), recordings.toList())
            adapter = recentClipsAdapter
        }
    }

    /**
     * Initialize the recording and playback dialog
     */
    @SuppressLint("SetTextI18n")
    private fun initDialog() {
        // Inflating dialog binding
        _dialogBinding = RecordAudioDialogueBinding.inflate(layoutInflater)

        // Creating the dialog
        dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)

            // Set dialog dimensions
            window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

            dialogBinding.word.text = "\"${targetWord}\""

            dialogBinding.imgClose.setOnClickListener {
                player.stop()
                cancel()
                recordingAdapter.notifyDataSetChanged()
            }

            dialogBinding.record.setOnClickListener {
                addRecord()
            }

            dialogBinding.playStop.setOnClickListener {
                playStop()
            }

            setCancelable(false)
        }
    }

    /**
     * Start or stop recording based on the current state.
     */
    @SuppressLint("SetTextI18n")
    private fun addRecord() {
        if (checkPermissions()) {
            // Check if necessary permissions are granted
            if (isRecording) {
                // If already recording, stop recording and update UI
                stopDrawing(dialogBinding.recorderVisualizer)
                recorder.stopRecording()
                dialogBinding.textViewRecord.text = "Record"
                isRecording = false
                recordingAdapter.notifyDataSetChanged()
                return
            }

            // Check existing audio files for the target word
            databaseHelper.getAllAudioFiles().forEach {
                while (it.endsWith("${targetWord}.mp3")) {
                    // If a recording for *targetWord* already exists
                    if (targetWord.contains("_")) {
                        val parts = targetWord.split("_")
                        targetWord = parts.getOrNull(0)!!
                        counter = parts.getOrNull(1)!!.toIntOrNull()!!
                    }
                    counter++
                    targetWord = "${targetWord}_$counter"
                }
            }

            // Create a new audio file for recording
            audioFile = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath,
                "${targetWord}.mp3"
            )

            filePath = audioFile?.absolutePath

            // Start recording
            recorder.startRecording(audioFile ?: return)
            dialogBinding.textViewRecord.text = "Recording"

            // Start drawing the visualizer
            startDrawing(recorder, dialogBinding.recorderVisualizer)

            // Add the new recording to the database
            databaseHelper.addAudioRecording(filePath ?: return)
            recordingAdapter.notifyDataSetChanged()
            isRecording = true
        } else
            // If permissions are not granted, request them
            requestPermissions()
    }

    /**
     * Start or stop playback based on the current state.
     */
    @SuppressLint("SetTextI18n")
    private fun playStop() {
        if (isRecording) {
            // If currently recording, stop recording and update UI
            stopDrawing(dialogBinding.recorderVisualizer)
            recorder.stopRecording()
            dialogBinding.textViewRecord.text = "Record"
            isRecording = false
            recordingAdapter.notifyDataSetChanged()
        }

        // Get all audio files and find the one related to the target word
        val recordings = hashSetOf<String>().also { it.addAll(databaseHelper.getAllAudioFiles()) }
        recordings.forEach {
            if (!it.endsWith("${targetWord}.mp3"))
                return@forEach
            else
                audioFile = File(it)
        }

        // If file doesn't exist
        if (audioFile == null || !audioFile?.exists()!! || audioFile?.length()!! <= 0) {
            Toast.makeText(requireContext(), "Record an audio for \"$targetWord\" first", Toast.LENGTH_LONG).show()
            return
        }

        // Set up visualizer for playback
        val playerVisualizer = dialogBinding.playerVisualizer

        // Set completion listener to handle the end of playback
        player.completionListener = MediaPlayer.OnCompletionListener {
            player.stop()
            dialogBinding.textViewPlayStop.text = "Play"
            dialogBinding.imageViewPlayStop.setImageResource(R.drawable.baseline_play_arrow_24_black)
        }

        // If already playing, stop playback
        if (player.isPlaying()) {
            player.stop()
            playerVisualizer.also { it.onStop() }.imgPause.performClick()
            dialogBinding.textViewPlayStop.text = "Play"
            dialogBinding.imageViewPlayStop.setImageResource(R.drawable.baseline_play_arrow_24_black)
            return
        }

        // Start playback
        player.play(audioFile ?: return)
        dialogBinding.textViewPlayStop.text = "Stop"
        dialogBinding.imageViewPlayStop.setImageResource(R.drawable.baseline_pause_24)

        // Set up visualizer for playback
        playerVisualizer.apply {
            setAudio(audioFile!!.absolutePath)
            imgPlay.performClick()
        }
    }

    /**
     * Start drawing the visualizer for audio recording.
     */
    private fun startDrawing(recorder: AudioRecorder, audioRecordView: AudioRecordView) {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                val currentMaxAmplitude = recorder.getMaxAmplitude()
                audioRecordView.update(currentMaxAmplitude) //redraw view
            }
        }, 0, 100)
    }

    /**
     * Stop drawing the visualizer for audio recording.
     */
    private fun stopDrawing(audioRecordView: AudioRecordView) {
        timer?.cancel()
        audioRecordView.recreate()
    }

    /**
     * Callback for handling permission requests.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // this method is called when user will
        // grant the permission for audio recording.
        when (requestCode) {
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.isNotEmpty()) {
                val permissionToRecord = grantResults[0] === PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] === PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {
                    Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        // this method is used to check permission
        val storagePermissionResult = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val microphonePermissionResult = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        )
        return storagePermissionResult == PackageManager.PERMISSION_GRANTED && microphonePermissionResult == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_AUDIO_PERMISSION_CODE
        )
    }

    override fun onPause() {
        super.onPause()

        // Save current value of counter for targetWord
        val sharedPreferences =
            requireContext().getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE)
        sharedPreferences.edit().also {
            it.putInt(targetWord, counter)
            it.apply()
        }
    }

    override fun onResume() {
        super.onResume()

        // Restore previous value of counter for targetWord
        counter = requireContext().getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE)
            .getInt(targetWord, 0)
    }

    override fun onDestroy() {
        _dialogBinding = null
        _fragmentBinding = null

        super.onDestroy()
    }

    companion object {
        private const val REQUEST_AUDIO_PERMISSION_CODE = 1
        private var counter = 0
        private const val SHARED_PREFERENCE_NAME = "counter"
    }
}