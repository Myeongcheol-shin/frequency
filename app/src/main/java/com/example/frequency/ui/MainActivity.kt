package com.example.frequency.ui

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frequency.R
import com.example.frequency.data.Music
import com.example.frequency.databinding.ActivityMainBinding
import com.example.frequency.rc.PlayListAdapter
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    lateinit var playListAdapter: PlayListAdapter
    private var model: PlayerModel = PlayerModel()
    private val vm: MainViewModel by viewModels()
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.main = vm


        initPlayView()
        initPlayListButton()
        initPlayControlButton()
        initSeekBar()
        initRecyclerView()
        model = PlayerModel(
            playMusicList = setMusic().mapIndexed{
                index, music -> music
            }
        )
        setMusicList(model.getAdapterModels())
    }

    private fun checkPermission()
    {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener{
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(this@MainActivity, "권한을 허가해주세요", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionGranted() {
                    setMusicVis()
                }
            })
            .setDeniedMessage("권한을 허용해주세요")
            .setPermissions(android.Manifest.permission.RECORD_AUDIO)
            .check()
    }

    private fun setMusicVis()
    {
        binding.musicVisual.setPlayer(player!!.audioSessionId)
    }
    private fun setMusic() : MutableList<Music>
    {
        val freq = mutableListOf<Music>()
        freq.add(
            Music(
                id = 0,
                name = "키 커지는 주파수",
                streamUrl = Uri.parse("android.resource://${packageName}/${R.raw.music}")
            )
        )
        freq.add(
            Music(
                id = 1,
                name = "키 작아지는 주파수",
                streamUrl = Uri.parse("android.resource://${packageName}/${R.raw.music}")
            )
        )
        return freq
    }
    private fun initPlayListButton() {
        binding.playlistImageView.setOnClickListener {

            // 서버에서 데이터가 다 불려오지 못했을 때 전환하지 않고 예외처리
            if (model.currentPosition == -1) return@setOnClickListener

            binding.playerViewGroup.isVisible = model.isWatchingPlayListView
            binding.playListViewGroup.isVisible = model.isWatchingPlayListView.not()

            model.isWatchingPlayListView = !model.isWatchingPlayListView
        }
    }

    private fun initRecyclerView() {
        playListAdapter = PlayListAdapter {
            // 음악 재생
            playMusic(it)
        }

        binding.playListRecyclerView.apply {
            adapter = playListAdapter
            layoutManager = LinearLayoutManager(context)
        }
        playListAdapter.submitList(model.getAdapterModels())

    }

    private var player: SimpleExoPlayer? = null

    private fun setMusicList(modelList : List<Music>) {
        applicationContext?.let {
            player?.addMediaItems(modelList.map { musicModel ->
                MediaItem.Builder()
                    .setMediaId(musicModel.id.toString())
                    .setUri(musicModel.streamUrl)
                    .build()
            })

            player?.prepare()
        }
    }

    private fun initPlayView() {
        applicationContext?.let {
            player = SimpleExoPlayer.Builder(it).build()
        }

        binding.playerView.player = player

        binding?.let { binding ->

            player?.addListener(object : Player.EventListener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)

                    if (isPlaying) {
                        binding.playControlImageView.setImageResource(R.drawable.icon_pause)
                    } else {
                        binding.playControlImageView.setImageResource(R.drawable.icon_play)
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)

                    updateSeek()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)

                    val newIndex = mediaItem?.mediaId ?: return
                    model.currentPosition = newIndex.toInt()
                    updatePlayerView(model.currentMusicModel())

                    playListAdapter.submitList(model.getAdapterModels())
                }
            })
        }
    }
    private fun initPlayControlButton() {

        binding.playControlImageView.setOnClickListener {
            val player = this.player ?: return@setOnClickListener

            if (player.isPlaying) {
                setMusicVis()
                player.pause()
            } else {
                player.play()
            }
        }

        binding.skipNextImageView.setOnClickListener {
            val nextMusic = model.nextMusic() ?: return@setOnClickListener
            playMusic(nextMusic)
        }

        binding.skipPrevImageView.setOnClickListener {
            val prevMusic = model.prevMusic() ?: return@setOnClickListener
            playMusic(prevMusic)
        }
    }

    private fun playMusic(musicModel: Music) {
        model.updateCurrentPosition(musicModel)
        player?.seekTo(model.currentPosition, 0)
        player?.play()

        checkPermission()
    }
    private val updateSeekRunnable = Runnable {
        updateSeek()
    }

    private fun updateSeek() {
        val player = this.player ?: return
        val duration = if (player.duration >= 0) player.duration else 0
        val position = player.currentPosition

        updateSeekUi(duration, position)

        val state = player.playbackState

        binding.root?.removeCallbacks(updateSeekRunnable)
        // 재생중일 때 (재생중이 아니거나 and 재생이 끝나지 않은 경우)
        if (state != Player.STATE_IDLE && state != Player.STATE_ENDED) {
            binding.root?.postDelayed(updateSeekRunnable, 1000)
        }
    }

    private fun updateSeekUi(duration: Long, position: Long) {

        binding?.let { binding ->

            binding.playListSeekBar.max = (duration / 1000).toInt()
            binding.playListSeekBar.progress = (position / 1000).toInt()

            binding.playerSeekBar.max = (duration / 1000).toInt()
            binding.playerSeekBar.progress = (position / 1000).toInt()

            binding.playTimeTextView.text = String.format("%02d:%02d",
                TimeUnit.MINUTES.convert(position, TimeUnit.MILLISECONDS),
                (position / 1000) % 60)
            binding.totalTimeTextView.text = String.format("%02d:%02d",
                TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS),
                (position / 1000) % 60)
        }
    }

    private fun initSeekBar() {
        binding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                player?.seekTo((seekBar.progress * 1000).toLong())
            }

        })

        binding.playListSeekBar.setOnTouchListener { view, motionEvent ->
            false
        }
    }


    private fun updatePlayerView(currentMusicModel: Music?) {
        currentMusicModel ?: return

        binding?.let { binding ->
            binding.trackTextView.text = currentMusicModel.name
        }
    }

    override fun onStop() {
        super.onStop()

        player?.pause()
        binding.root.removeCallbacks(updateSeekRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()

        player?.release()
        binding.root?.removeCallbacks(updateSeekRunnable)
    }
}

