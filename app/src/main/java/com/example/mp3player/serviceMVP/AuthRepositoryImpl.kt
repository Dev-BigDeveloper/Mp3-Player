package com.example.mp3player.serviceMVP

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class AuthRepositoryImpl : AuthRepository {

    override suspend fun logInAsync(name: String, password: String): Deferred<String> {
        return GlobalScope.async {
            Thread.sleep(3000)
            ""
        }
    }
}