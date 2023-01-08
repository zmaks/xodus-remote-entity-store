package com.zheltoukhov.xres.client.connection

import com.zheltoukhov.xres.protocol.Protocol
import io.ktor.network.sockets.*
import java.io.Closeable

class Connection(
    private val socketFactory: SocketFactory
) : Closeable {

    private lateinit var socket: Socket

    suspend fun connect(): Protocol {
        socket = socketFactory.create()
        println("connect ${socket.localAddress}")
        return Protocol(socket.openWriteChannel(), socket.openReadChannel())
    }

    override fun close() {
        if (isConnected()) {
            val ra = socket.localAddress
            socket.close()
            println("close $ra")
        }
    }

    fun isConnected() = this::socket.isInitialized && !socket.isClosed
}