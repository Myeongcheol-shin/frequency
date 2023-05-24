package com.example.frequency.rc

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.example.frequency.R
import com.example.frequency.data.Music

class MusicListRecyclerView (private val context: Context, private val datas : List<Music>,private val itemClickEvent : (Music) -> Unit) : RecyclerView.Adapter<MusicListRecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicListRecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.music_list_item, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: MusicListRecyclerView.ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.tv)

        init {
        }
        fun bind(item : Music) {
            name.apply {
                text = item.name

                setOnClickListener {
                    itemClickEvent(item)
                }
            }
        }

    }
}