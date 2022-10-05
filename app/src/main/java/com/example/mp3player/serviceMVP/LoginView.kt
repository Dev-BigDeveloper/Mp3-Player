package com.example.mp3player.serviceMVP

import com.example.mp3player.adaptersMusic.Music

interface LoginView {
    fun showMusics(list:List<Music>)

    fun showError(message:String)

    fun onPermissionDenied()

}