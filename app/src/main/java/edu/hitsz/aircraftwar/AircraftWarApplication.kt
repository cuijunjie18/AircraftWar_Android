package edu.hitsz.aircraftwar

import android.app.Application
import android.content.Context

class AircraftWarApplication : Application(){
  companion object {
    const val TAG = "AircraftWarApplication"
    lateinit var context: Context
    const val SCREEN_WIDTH = 512
    const val SCREEN_HEIGHT = 768
  }
  override fun onCreate() {
    super.onCreate()
    context = applicationContext
  }
}