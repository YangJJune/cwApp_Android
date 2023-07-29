package com.ysj.cwclanapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ysj.cwclanapp.databinding.RankrowBinding
import java.text.FieldPosition

class ladderAdapter(val data:ArrayList<userData>):
    RecyclerView.Adapter<ladderAdapter.ViewHolder>() {
    interface OnItemClickListener{
        fun OnItemClick(position: Int, player:userData)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class ViewHolder(val binding: RankrowBinding):RecyclerView.ViewHolder(binding.root){
        init{
            binding.rankIDView.setOnClickListener {
                //플레이어 정보로 이동
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = RankrowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.rankNumView.text = data[position].rankNum.toString()
        holder.binding.rankIDView.text = data[position].id
        holder.binding.RankELOView.text = data[position].elo.toString()
    }
}