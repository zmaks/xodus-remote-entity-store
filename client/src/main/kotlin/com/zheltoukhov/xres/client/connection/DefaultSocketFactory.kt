package com.zheltoukhov.xres.client.connection

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers

class DefaultSocketFactory(
    private val config: SocketConfig? = null
) : SocketFactory {

    companion object {
        const val DEFAULT_HOST = "127.0.0.1"
        const val DEFAULT_PORT = 9042
    }

    override suspend fun create(): Socket {
        val host = config?.host ?: DEFAULT_HOST
        val port = config?.port ?: DEFAULT_PORT
        val selectorManager = SelectorManager(Dispatchers.IO)
        return aSocket(selectorManager).tcp().connect(host, port)
    }
}