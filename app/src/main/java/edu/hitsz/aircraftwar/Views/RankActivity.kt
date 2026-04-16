package edu.hitsz.aircraftwar.Views

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.data.DataManager
import edu.hitsz.aircraftwar.setting.Setting

class RankActivity : BaseActivity() {
  @SuppressLint("MissingInflatedId")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_rank)

    val difficulty: String = Setting.getDifficulty()

    // 设置难度文字
    val rankDifficulty: TextView = findViewById(R.id.textForDifficulty)
    when (difficulty) {
      "easy" -> rankDifficulty.text = "难度：简单"
      "medium" -> rankDifficulty.text = "难度：中等"
      "hard" -> rankDifficulty.text = "难度：困难"
    }

    // 设置 RecyclerView
    val scoresRecyclerView: RecyclerView = findViewById(R.id.scoreRecyclerView)
    scoresRecyclerView.layoutManager = LinearLayoutManager(this)

    // 添加分割线
    scoresRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
      private val paint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 1f
      }
      override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft + 16
        val right = parent.width - parent.paddingRight - 16
        for (i in 0 until parent.childCount - 1) {
          val child = parent.getChildAt(i)
          val top = child.bottom.toFloat()
          c.drawLine(left.toFloat(), top, right.toFloat(), top, paint)
        }
      }
    })

    val scores = DataManager.loadData(difficulty)
    val adapter = ScoreAdapter(scores)
    scoresRecyclerView.adapter = adapter
  }
}