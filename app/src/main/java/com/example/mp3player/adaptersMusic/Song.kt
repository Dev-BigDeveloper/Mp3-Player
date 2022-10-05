package com.example.mp3player.adaptersMusic

import android.net.Uri

data class Song(
    val title:String,
    val uri: Uri,
    val artworkUri:Uri,
    val size:Int,
    val duration:Int
)
