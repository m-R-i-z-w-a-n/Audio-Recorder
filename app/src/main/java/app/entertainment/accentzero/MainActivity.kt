package app.entertainment.accentzero

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.WindowManager
import app.entertainment.accentzero.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the Status Bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(binding.root)

        setAppTitle()

        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment == null)
        // Add the fragment to the activity
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, MainFragment())
                commit()
            }

        // Set up click listeners for category buttons
        handleCategoryButtonClick()
    }

    // Set a styled title for the app
    private fun setAppTitle() {
        val spannableString = SpannableString(resources.getString(R.string.app_name))
        val boldSpan = StyleSpan(Typeface.BOLD)
        // Apply bold style to a portion of the title
        spannableString.setSpan(boldSpan, 1, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.appTitle.text = spannableString
    }

    private fun handleCategoryButtonClick() {
        binding.btnSkills.setOnClickListener {
            arrayOf(binding.btnClips, binding.btnJoinFree).forEach {
                // Reset styles for other buttons
                it.setTextColor(Color.WHITE)
                it.background.setTint(resources.getColor(R.color.base_color, resources.newTheme()))
            }

            // Apply selected styles to the Skills button
            binding.btnSkills.apply {
                setTextColor(resources.getColor(R.color.base_color, resources.newTheme()))
                background.setTint(Color.WHITE)
            }
        }

        binding.btnClips.setOnClickListener {
            // Reset styles for other buttons
            arrayOf(binding.btnSkills, binding.btnJoinFree).forEach {
                it.setTextColor(Color.WHITE)
                it.background.setTint(resources.getColor(R.color.base_color, resources.newTheme()))
            }

            // Apply selected styles to the Clips button
            binding.btnClips.apply {
                setTextColor(resources.getColor(R.color.base_color, resources.newTheme()))
                background.setTint(Color.WHITE)
            }

            // Replace the fragment with ClipsFragment and add to back stack
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, ClipsFragment())
                addToBackStack(null)
                commit()
            }
        }

        binding.btnJoinFree.setOnClickListener {
            // Reset styles for other buttons
            arrayOf(binding.btnSkills, binding.btnClips).forEach {
                it.setTextColor(Color.WHITE)
                it.background.setTint(resources.getColor(R.color.base_color, resources.newTheme()))
            }

            // Apply selected styles to the Join Free button
            binding.btnJoinFree.apply {
                setTextColor(resources.getColor(R.color.base_color, resources.newTheme()))
                background.setTint(Color.WHITE)
            }
        }
    }
}