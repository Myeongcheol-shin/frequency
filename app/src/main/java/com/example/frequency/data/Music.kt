package com.example.frequency.data

import android.net.Uri

data class Music (
    val id : Long,
    val name : String,
    val streamUrl : Uri?,
    val isPlaying : Boolean = false
    )