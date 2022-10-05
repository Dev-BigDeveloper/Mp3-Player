package com.example.mp3player.serviceMVP

import kotlinx.coroutines.Deferred

interface AuthRepository {
    suspend fun logInAsync(name:String, password:String):Deferred<String>
}