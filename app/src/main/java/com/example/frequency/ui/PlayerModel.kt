package com.example.frequency.ui

import com.example.frequency.data.Music

data class PlayerModel(
    private val playMusicList: List<Music> = emptyList(),
    var currentPosition: Int = -1,
    var isWatchingPlayListView: Boolean = true
) {

    fun getAdapterModels(): List<Music> {
        return playMusicList.mapIndexed { index, music ->
            val newItem = music.copy(
                isPlaying = index == currentPosition
            )
            newItem
        }
    }

    fun updateCurrentPosition(musicModel: Music) {
        currentPosition = playMusicList.indexOf(musicModel)
    }

    fun nextMusic(): Music? {
        if (playMusicList.isEmpty()) return null

        currentPosition = if ((currentPosition + 1) == playMusicList.size) 0 else currentPosition + 1
        return playMusicList[currentPosition]
    }

    fun prevMusic(): Music? {
        if (playMusicList.isEmpty()) return null

        currentPosition = if ((currentPosition - 1) < 0) playMusicList.lastIndex else currentPosition - 1
        return playMusicList[currentPosition]
    }

    fun currentMusicModel(): Music? {
        if (playMusicList.isEmpty()) return null

        return playMusicList[currentPosition]
    }
}