package com.zheltoukhov.xres.protocol.command

import com.zheltoukhov.xres.protocol.*

abstract class Command<O : RequestPayload, I : ResponsePayload> {

    suspend fun writeRequest(request: Request<O>, protocol: Protocol) {
        protocol.writeHeader(request.header)
        writeRequestPayload(request.payload, protocol)
    }


    suspend fun readResponse(protocol: Protocol): Response<I> {
        val header = protocol.readHeader()
        val payload = readResponsePayload(protocol)
        return Response(header, payload)
    }


    protected abstract suspend fun writeRequestPayload(payload: O, protocol: Protocol)

    protected abstract suspend fun readResponsePayload(protocol: Protocol): I

}