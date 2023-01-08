package com.zheltoukhov.xres.protocol.command

import com.zheltoukhov.xres.protocol.*
import com.zheltoukhov.xres.protocol.dto.ErrorDto
import com.zheltoukhov.xres.protocol.exception.CommunicationException
import com.zheltoukhov.xres.protocol.exception.CommandErrorException

abstract class Command<O : RequestPayload, I : ResponsePayload> {

    suspend fun writeRequest(request: Request<O>, protocol: Protocol) {
        try {
            protocol.writeRequestHeader(request.header)
            request.payload?.let { writeRequestPayload(it, protocol) }
            protocol.flush()
        } catch (e: CommandErrorException) {
            throw e
        } catch (e: Exception) {
            throw CommunicationException("Cannot write request [requestId=${request.header.requestId}]", e)
        }
    }

    suspend fun readResponse(protocol: Protocol): Response<I> {
        try {
            protocol.awaitResponse()
            val header = protocol.readResponseHeader()
            if (header.isError) {
                val errorDto = protocol.readError()
                throw CommandErrorException(errorDto.message)
            }
            val payload = readResponsePayload(protocol)
            return Response(header, payload)
        } catch (e: CommandErrorException) {
            throw e
        } catch (e: Exception) {
            throw CommunicationException("Cannot read response", e)
        }
    }

    suspend fun writeResponse(response: Response<I>, protocol: Protocol) {
        try {
            protocol.writeResponseHeader(response.header)
            response.payload?.let { writeResponsePayload(it, protocol) }
            protocol.flush()
        } catch (e: CommandErrorException) {
            throw e
        } catch (e: Exception) {
            throw CommunicationException("Cannot write response [requestId=${response.header.requestId}]", e)
        }
    }

    suspend fun readRequest(protocol: Protocol): Request<O> {
        try {
            protocol.awaitResponse()
            val header = protocol.readRequestHeader()
            val payload = readRequestPayload(protocol)
            return Request(header, payload)
        } catch (e: CommandErrorException) {
            throw e
        } catch (e: Exception) {
            throw CommunicationException("Cannot read request", e)
        }
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