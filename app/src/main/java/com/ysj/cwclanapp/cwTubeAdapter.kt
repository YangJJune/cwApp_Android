package com.ysj.cwclanapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ysj.cwclanapp.databinding.VideorowBinding

class cwTubeAdapter(val data:ArrayList<videoData>, val context:Context):
    RecyclerView.Adapter<cwTubeAdapter.ViewHolder>() {
    interface OnItemClickListener{
        fun OnItemClick(position: Int, code:String)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class ViewHolder(val binding: VideorowBinding):RecyclerView.ViewHolder(binding.root){
        init{
            val activity = context as MainActivity
            binding.videoTitle.setOnClickListener {
                //영상으로 이동
                val pos = this.adapterPosition
                val link = data[pos].link.toString()
                val intent = Intent(Intent.ACTION_VIEW,Uri.parse(link))
                Log.d("test",link)
                activity.startActivity(intent)
            }
            binding.videoThumbnail.setOnClickListener {
                //영상으로 이동
                val pos = this.adapterPosition
                val link = data[pos].link.toString()
                val intent = Intent(Intent.ACTION_VIEW,Uri.parse(link))
                activity.startActivity(intent)
            }
            binding.videoAuthor.setOnClickListener {
                //해당 유튜브 / 방송국으로 이동
                val author = data[this.adapterPosition].author
                when(author){
                    "CW동물원"->{
                        val intent = Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com/@makingsignature5609"))
                        activity.startActivity(intent)
                    }
                    "두미★둠"->{
                        val intent = Intent(Intent.ACTION_VIEW,Uri.parse("https://bj.afreecatv.com/taijizoom"))
                        activity.startActivity(intent)
                    }
                }
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
        when(data[position].author){
            "CW동물원"->{
                holder.binding.imageView3.setImageResource(R.drawable.zebratube)
            }
            "두미★둠"->{
                holder.binding.imageView3.setImageResource(R.drawable.tazijoom)
            }
        }
        holder.binding.imageView3.clipToOutline = true
    }
}