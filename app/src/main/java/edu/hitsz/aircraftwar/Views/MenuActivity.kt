package edu.hitsz.aircraftwar.Views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import edu.hitsz.aircraftwar.MainActivity
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.logic.difficulty.*
import edu.hitsz.aircraftwar.setting.Setting

class MenuActivity: AppCompatActivity() {
  private lateinit var easyButton: Button
  private lateinit var mediumButton: Button
  private lateinit var hardButton: Button
  private lateinit var musicSwitch: Switch
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_menu)

    Setting.init()

    setupButtons()
    setupMusicSwitch()
  }

  private fun setupButtons() {
    easyButton = findViewById(R.id.buttonForSimple)
    mediumButton = findViewById(R.id.buttonForMedium)
    hardButton = findViewById(R.id.buttonForHard)
    val listener = View.OnClickListener { view ->
      handleButtonClick(view)
    }
    easyButton.setOnClickListener(listener)
    mediumButton.setOnClickListener(listener)
    hardButton.setOnClickListener(listener)
  }

  private fun setupMusicSwitch() {
    musicSwitch = findViewById(R.id.switch_music)
    musicSwitch.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        Setting.musicOpen = true
      } else {
        Setting.musicOpen = false
      }
    }
  }

  private fun handleButtonClick(view: View) {
    when (view.id) {
      R.id.buttonForSimple -> {
        Setting.difficulty = Easy()
      }
      R.id.buttonForMedium -> {
        Setting.difficulty = Medium()
      }
      R.id.buttonForHard -> {
        Setting.difficulty = Hard()
      }
    }
    startGame()
  }

  private fun startGame() {
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
  }
}