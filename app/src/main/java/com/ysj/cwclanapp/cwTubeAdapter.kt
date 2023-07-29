package com.ysj.cwclanapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ysj.cwclanapp.databinding.VideorowBinding

class cwTubeAdapter(val data:ArrayList<videoData>, val context:Context):
    RecyclerView.Adapter<cwTubeAdapter.ViewHolder>() {
    interface OnItemClickListener{
        fun OnItemClick(position: Int, player:userData)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class ViewHolder(val binding: VideorowBinding):RecyclerView.ViewHolder(binding.root){
        init{
            binding.videoTitle.setOnClickListener {
                //영상으로 이동
            }
            binding.videoThumbnail.setOnClickListener {
                //영상으로 이동
            }
            binding.videoAuthor.setOnClickListener {
                //해당 유튜브 / 방송국으로 이동
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = VideorowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(data[position].img).into(holder.binding.videoThumbnail)
        holder.binding.videoAuthor.text = data[position].author
        holder.binding.videoTitle.text = data[position].title

    }
}