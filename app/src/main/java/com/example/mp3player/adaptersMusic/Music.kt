package com.example.mp3player.adaptersMusic

import java.io.Serializable

data class Music(
    var name: String = "",
    var albums: String = "",
    var artist: String = "",
    var duration: String = "",
    var uslString: String = "",
    var albumId: String = "",
    var imagePath:String = ""
) : Serializable
