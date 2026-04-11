package edu.hitsz.aircraftwar.Views

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.data.DataManager
import edu.hitsz.aircraftwar.data.SingleGameInfo
import edu.hitsz.aircraftwar.setting.Setting

class RankActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_rank)
    val scoresRecyclerView: RecyclerView = findViewById<RecyclerView>(R.id.scoreRecyclerView)
    val layoutManager = LinearLayoutManager(this)
    scoresRecyclerView?.layoutManager = layoutManager

    val difficulty: String = Setting.getDifficulty()
    val scores: List<SingleGameInfo> = DataManager.loadData(difficulty)
    val adapter = ScoreAdapter(scores)
    scoresRecyclerView?.adapter = adapter

    val rankName:TextView = findViewById<TextView>(R.id.textForRank)
    rankName.text = "排行榜: $difficulty"
  }
}