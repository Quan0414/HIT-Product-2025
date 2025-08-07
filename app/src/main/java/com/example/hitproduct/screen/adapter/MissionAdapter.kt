package com.example.hitproduct.screen.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hitproduct.R
import com.example.hitproduct.data.model.mission.Mission

class MissionAdapter(
    private val items: MutableList<Mission> = mutableListOf()
) : RecyclerView.Adapter<MissionAdapter.MissionViewHolder>() {

    fun submitList(newMissions: List<Mission>) {
        items.clear()
        items.addAll(newMissions)
        notifyDataSetChanged()
    }

    inner class MissionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.tvContentMission)
        private val checkBox = view.findViewById<ImageView>(R.id.imgComplete)
        private val coin = view.findViewById<TextView>(R.id.tvMissionCoin)

        fun bind(mission: Mission) {
            val ctx = itemView.context
            title.text = when (mission.missionId.key) {
                "daily_login" -> ctx.getString(R.string.login)
                "message_partner" -> ctx.getString(R.string.mess, mission.countCompleted)
                "feed_pet" -> ctx.getString(R.string.feed_pet, mission.countCompleted)
                else -> ctx.getString(R.string.daily_question, mission.countCompleted)
            }
            coin.text = "+ ${mission.missionId.coin} đồng"
            checkBox.setImageResource(
                if (mission.isCompleted) {
                    R.drawable.ic_mission_complete
                } else {
                    R.drawable.ic_mission_incomplete
                }
            )

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission, parent, false)
        return MissionViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        holder.bind(items[position])
    }
}