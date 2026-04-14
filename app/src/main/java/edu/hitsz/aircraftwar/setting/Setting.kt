package edu.hitsz.aircraftwar.setting

import android.util.Log
import edu.hitsz.aircraftwar.logic.difficulty.*

object Setting {
  public var difficulty: Difficulty = Easy()
  public var musicOpen: Boolean = false
  public var userName: String = "Default"
  public var onlineMode: Boolean = false

  fun init() {
    Log.d("Setting", "init")
  }

  public fun getDifficulty(): String {
    when (difficulty) {
      is Easy -> return "easy"
      is Medium -> return "medium"
      is Hard -> return "hard"
    }
    return "easy"
  }
}