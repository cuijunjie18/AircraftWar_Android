package edu.hitsz.aircraftwar.Views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.logic.difficulty.*
import edu.hitsz.aircraftwar.setting.Music.MusicManager
import edu.hitsz.aircraftwar.setting.Setting
import edu.hitsz.aircraftwar.view.activity.GameActivity
import edu.hitsz.aircraftwar.view.activity.OnlineGameActivity

class MenuActivity: BaseActivity() {
  private lateinit var easyButton: Button
  private lateinit var mediumButton: Button
  private lateinit var hardButton: Button
  private lateinit var onlineButton: Button
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
    onlineButton = findViewById(R.id.buttonForOnline)
    val listener = View.OnClickListener { view ->
      handleButtonClick(view)
    }
    easyButton.setOnClickListener(listener)
    mediumButton.setOnClickListener(listener)
    hardButton.setOnClickListener(listener)
    onlineButton.setOnClickListener(listener)
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
      R.id.buttonForOnline -> {
        Setting.onlineMode = true
      }
    }
    startGame()
  }

  private fun startGame() {
    when (Setting.onlineMode) {
      true -> { startActivity(Intent(this, OnlineGameActivity::class.java))}
      false -> { startActivity(Intent(this, GameActivity::class.java)) }
    }
  }
}