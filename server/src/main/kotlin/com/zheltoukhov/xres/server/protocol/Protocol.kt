package com.zheltoukhov.xres.server.protocol

import io.ktor.utils.io.*

interface Protocol {

    suspend fun readRequest(channel: ByteReadChannel): Request

    suspend fun writeResponse(channel: ByteWriteChannel, response: Response)
}