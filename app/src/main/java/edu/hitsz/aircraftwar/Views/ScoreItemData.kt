package edu.hitsz.aircraftwar.Views

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.hitsz.aircraftwar.R
import edu.hitsz.aircraftwar.data.SingleGameInfo

class ScoreAdapter(val scoresList: List<SingleGameInfo>) :
  RecyclerView.Adapter<ScoreAdapter.ViewHolder>() {
  class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val rankNumberView: TextView = view.findViewById(R.id.rankNumber)
    val userNameView: TextView = view.findViewById(R.id.userName)
    val scoreValueView: TextView = view.findViewById(R.id.scoreValue)
    val timeInfoView: TextView = view.findViewById(R.id.timeInfo)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.score_item_data, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val scoreInfo = scoresList[position]
    val rank = position + 1

    // 排名显示，前三名用奖牌 emoji
    holder.rankNumberView.text = when (rank) {
      1 -> "🥇"
      2 -> "🥈"
      3 -> "🥉"
      else -> rank.toString()
    }

    holder.userNameView.text = scoreInfo.userName ?: "未知"
    holder.scoreValueView.text = scoreInfo.score.toString()
    holder.timeInfoView.text = scoreInfo.date ?: ""

    // 奇偶行不同背景色
    if (position % 2 == 0) {
      holder.itemView.setBackgroundColor(Color.WHITE)
    } else {
      holder.itemView.setBackgroundColor(Color.parseColor("#FAFAFA"))
    }
  }

  override fun getItemCount() = scoresList.size
}