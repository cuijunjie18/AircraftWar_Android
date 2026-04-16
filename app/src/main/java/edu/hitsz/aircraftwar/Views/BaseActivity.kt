package edu.hitsz.aircraftwar.Views

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import edu.hitsz.aircraftwar.ActivityCollector

open class BaseActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d("BaseActivity", "onCreate: ${javaClass.simpleName}")
    ActivityCollector.addActivity(this)
  }

  override fun onPause() {
    super.onPause()
    Log.d("BaseActivity", "onPause: ${javaClass.simpleName}")
  }

  override fun onResume() {
    super.onResume()
    Log.d("BaseActivity", "onResume: ${javaClass.simpleName}")
  }

  override fun onDestroy() {
    super.onDestroy()
    Log.d("BaseActivity", "onDestroy: ${javaClass.simpleName}")
    ActivityCollector.removeActivity(this)
  }
}