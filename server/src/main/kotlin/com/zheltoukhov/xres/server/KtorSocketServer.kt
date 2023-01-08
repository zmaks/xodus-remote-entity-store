package com.zheltoukhov.xres.server

import com.zheltoukhov.xres.protocol.Protocol
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KtorSocketServer(
    private val port: Int?,
    private val messageHandler: CommandHandler
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val DEFAULT_PORT = 9042
    }

    fun run() {
        runBlocking {
            val serverSocket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().bind(port = port ?: DEFAULT_PORT)
            log.info("Server listening at ${serverSocket.localAddress}")

            while (true) {
                val socket = serverSocket.accept()
                val remoteAddress = socket.remoteAddress
                log.debug("Accepted connection from $remoteAddress")
                launch {
                    try {
                        val read = socket.openReadChannel()
                        val write = socket.openWriteChannel()
                        messageHandler.handle(Protocol(write, read))
                    } catch (e: Throwable) {
                        log.error("Socket server error, closing connection $remoteAddress", e)
                    } finally {
                        socket.close()
                        socket.awaitClosed()
                        log.debug("Connection closed $remoteAddress")
                    }
                }

            }
        }
    }
}