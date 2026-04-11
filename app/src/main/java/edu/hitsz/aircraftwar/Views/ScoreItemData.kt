package edu.hitsz.aircraftwar.Views

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
    val rankTextView: TextView = view.findViewById(R.id.userName)
    val nameTextView: TextView = view.findViewById(R.id.scoreValue)
    val scoreTextView: TextView = view.findViewById(R.id.timeInfo)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.activity_rank, parent, false)
    val holder = ViewHolder(view)
    return holder
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val scoreInfo = scoresList[position]
  }

  override fun getItemCount() = scoresList.size
}