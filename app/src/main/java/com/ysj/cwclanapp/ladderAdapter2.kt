package com.ysj.cwclanapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ysj.cwclanapp.databinding.RankrowBinding
import com.ysj.cwclanapp.databinding.RecentrowBinding
import java.text.FieldPosition

class ladderAdapter2(val data:ArrayList<cwMatch>):
    RecyclerView.Adapter<ladderAdapter2.ViewHolder>() {
    interface OnItemClickListener{
        fun OnItemClick(position: Int, player:userData)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class ViewHolder(val binding: RecentrowBinding):RecyclerView.ViewHolder(binding.root){
        init{
            binding.Id1View.setOnClickListener {
                //플레이어 정보로 이동
            }

            binding.Id2View.setOnClickListener {
                //플레이어 정보로 이동
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = RecentrowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.Id1View.text = data[position].winner
        holder.binding.Id2View.text = data[position].loser
        holder.binding.dateView.text = data[position].date
        holder.binding.mapView.text = data[position].map
    }
}