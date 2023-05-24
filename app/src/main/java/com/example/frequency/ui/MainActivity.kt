package com.example.frequency.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.frequency.R
import com.example.frequency.data.Music
import com.example.frequency.databinding.ActivityMainBinding
import com.example.frequency.rc.MusicListRecyclerView

class MainActivity : AppCompatActivity() {
    private val vm: MainViewModel by viewModels()
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var musicListRecyclerView: MusicListRecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.main = vm

        setMusicAdapter()

        binding.apply {
            closeBtn.setOnClickListener {
                musicListGroup.visibility = View.VISIBLE
                player.visibility = View.GONE
            }

            musicListRc.apply {
                layoutManager =  LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL,false)
                adapter = musicListRecyclerView
                notifyChange()
            }

        }


    }

    private fun setMusicAdapter(){
        val freqList = mutableListOf<Music>()

        resources.getStringArray(R.array.freqList).forEach {
            freqList.add(Music(it))
        }

        musicListRecyclerView = MusicListRecyclerView(
            context = this,
            datas = freqList,
            itemClickEvent = {
                binding.apply {
                    musicListGroup.visibility = View.GONE
                    player.visibility = View.VISIBLE
                    musicName.text = it.name
                }
            }
        )
    }
}