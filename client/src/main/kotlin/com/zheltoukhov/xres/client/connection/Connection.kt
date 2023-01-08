package com.zheltoukhov.xres.client.connection

import com.zheltoukhov.xres.protocol.Protocol
import io.ktor.network.sockets.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable

class Connection(
    private val socketFactory: SocketFactory
) : Closeable {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private lateinit var socket: Socket

    suspend fun connect(): Protocol {
        socket = socketFactory.create()
        log.debug("Connected to {}", socket.localAddress)
        return Protocol(socket.openWriteChannel(), socket.openReadChannel())
    }

    override fun close() {
        if (isConnected()) {
            val remoteAddress = socket.remoteAddress
            socket.close()
            log.debug("Closed connection to {}", remoteAddress)
        }
    }

    fun isConnected() = this::socket.isInitialized && !socket.isClosed
}