package com.zheltoukhov.xres.protocol.command

import com.zheltoukhov.xres.protocol.*

abstract class Command<O : RequestPayload, I : ResponsePayload> {

    suspend fun writeRequest(request: Request<O>, protocol: Protocol) {
        protocol.writeHeader(request.header)
        request.payload?.let { writeRequestPayload(it, protocol) }
        protocol.flush()
    }

    suspend fun readResponse(protocol: Protocol): Response<I> {
        val header = protocol.readHeader()
        val payload = readResponsePayload(protocol)
        return Response(header, payload)
    }

    suspend fun writeResponse(response: Response<I>, protocol: Protocol) {
        protocol.writeHeader(response.header)
        response.payload?.let { writeResponsePayload(it, protocol) }
        protocol.flush()
    }

    suspend fun readRequest(protocol: Protocol): Request<O> {
        val header = protocol.readHeader()
        val payload = readRequestPayload(protocol)
        return Request(header, payload)
    }

    protected open suspend fun writeRequestPayload(payload: O, protocol: Protocol) {

    }

    protected open suspend fun readResponsePayload(protocol: Protocol): I? {
        return null
    }

    protected open suspend fun writeResponsePayload(payload: I, protocol: Protocol) {

    }

    protected open suspend fun readRequestPayload(protocol: Protocol): O? {
        return null
    }

}