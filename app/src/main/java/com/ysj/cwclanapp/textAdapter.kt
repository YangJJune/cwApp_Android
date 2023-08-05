package com.ysj.cwclanapp

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ysj.cwclanapp.databinding.RankrowBinding
import com.ysj.cwclanapp.databinding.TextrowBinding
import java.text.FieldPosition

class textAdapter(val data:ArrayList<String>, val context:Context):
    RecyclerView.Adapter<textAdapter.ViewHolder>() {
    interface OnItemClickListener{
        fun OnItemClick(position: Int, str:String)
    }

    var itemClickListener:OnItemClickListener?=null
    inner class ViewHolder(val binding: TextrowBinding):RecyclerView.ViewHolder(binding.root){
        init{
            binding.textView11.setOnClickListener {
                val pos = this.adapterPosition
                val content = data[pos]
                val builder = AlertDialog.Builder(context)
                builder.setIcon(null).setMessage(content)
                builder.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = TextrowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.textView11.text = data[position].split("\n")[0]
    }
}