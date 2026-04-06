package edu.hitsz.aircraftwar

import android.app.Application
import android.content.Context

class AircraftWarApplication : Application(){
  companion object {
    const val TAG = "AircraftWarApplication"
    var SCREEN_WIDTH = 0
    var SCREEN_HEIGHT = 0
    lateinit var context: Context
  }
  override fun onCreate() {
    super.onCreate()
    context = applicationContext
    val metrics = resources.displayMetrics
    SCREEN_WIDTH = metrics.widthPixels
    SCREEN_HEIGHT = metrics.heightPixels
  }
}