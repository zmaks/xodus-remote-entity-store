package com.zheltoukhov.xres.server

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KtorSocketServer(
    private val port: Int?
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
                log.debug("Accepted connection from ${socket.remoteAddress}")

                launch {
                    val read = socket.openReadChannel()
                    val write = socket.openWriteChannel()
                    try {
                        while (true) {
                            //TODO
                            val line = read.readUTF8Line()
                            println(line)
                            write.writeStringUtf8("$line\n")
                            write.flush()
                        }
                    } catch (e: Throwable) {
                        log.error("Socket server error, closing connection ${socket.remoteAddress}", e)
                        withContext(Dispatchers.IO) {
                            socket.close()
                        }
                    }
                }
            }
        }
    }
}