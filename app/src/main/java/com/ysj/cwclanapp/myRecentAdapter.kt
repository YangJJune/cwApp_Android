package com.ysj.cwclanapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ysj.cwclanapp.databinding.Recentrow2Binding
import com.ysj.cwclanapp.databinding.RecentrowBinding

class myRecentAdapter (val data:ArrayList<myrecentData>):
    RecyclerView.Adapter<myRecentAdapter.ViewHolder>() {
        interface OnItemClickListener{
            fun OnItemClick(position: Int, player:userData)
        }

        var itemClickListener:OnItemClickListener?=null

        inner class ViewHolder(val binding:Recentrow2Binding): RecyclerView.ViewHolder(binding.root){
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
            val view = Recentrow2Binding.inflate(LayoutInflater.from(parent.context),parent,false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.Id1View.text = data[position].res
            if(data[position].res == "승"){
                holder.binding.Id1View.setTextColor(Color.GREEN)
                holder.binding.Id1View.text
            }
            else{

            }

            holder.binding.Id2View.text = "VS "+data[position].opp
            holder.binding.dateView.text = data[position].date
            holder.binding.mapView.text = data[position].map
        }
}