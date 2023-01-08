package com.zheltoukhov.xres.client.connection

import io.ktor.network.sockets.*

interface SocketFactory {

    suspend fun create(): Socket
}

data class SocketConfig(
    val host: String,
    val port: Int
)