package com.zheltoukhov.xres.server

import com.zheltoukhov.xres.protocol.*
import com.zheltoukhov.xres.protocol.command.Commands
import com.zheltoukhov.xres.protocol.dto.ErrorDto
import com.zheltoukhov.xres.protocol.exception.CommunicationException
import com.zheltoukhov.xres.server.transaction.TransactionManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class CommandHandler(
    private val provider: TransactionManager,
    private val commands: Commands = Commands()
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    suspend fun handle(protocol: Protocol) {
        val commandType = protocol.readCommandType()
        when (commandType) {
            CommandType.BEGIN_TX -> handleBeginTx(protocol, commandType)
            CommandType.ABORT -> handleAbort(protocol, commandType)
            CommandType.COMMIT -> handleCommit(protocol, commandType)
            CommandType.FLUSH -> handleFlush(protocol, commandType)

            CommandType.CREATE -> handleCreate(protocol, commandType)
            CommandType.UPDATE -> handleUpdate(protocol, commandType)
            CommandType.DELETE -> handleDelete(protocol, commandType)
            CommandType.GET -> handleGet(protocol, commandType)
            CommandType.FIND -> handleFind(protocol, commandType)
        }
    }

    private suspend fun handleBeginTx(protocol: Protocol, commandType: CommandType) {
        val request = commands.beginTx.readRequest(protocol)
        log.debug("Request received [command={}, requestId={}]", commandType, request.header.requestId)
        withErrorHandler(request, protocol) {
            if (request.header.txId != null) throw IllegalArgumentException(
                "$commandType cannot be executed within existing transaction ${request.header.txId} " +
                        "[requestId=${request.header.requestId}]"
            )
            val txn = provider.beginTransaction()
            val response = Response<EmptyPayload>(ResponseHeader(request.header.requestId, txn.getTransactionId()))
            commands.beginTx.writeResponse(response, protocol)
        }
        log.debug("Response has been sent [command={}, requestId={}]", commandType, request.header.requestId)
    }

    private suspend fun handleAbort(protocol: Protocol, commandType: CommandType) {
        val request = commands.abort.readRequest(protocol)
        log.debug("Request received [command={}, requestId={}]", commandType, request.header.requestId)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            provider.abort(txId)
            val response = Response<EmptyPayload>(ResponseHeader(request.header.requestId, txId))
            commands.abort.writeResponse(response, protocol)
        }
        log.debug("Response has been sent [command={}, requestId={}]", commandType, request.header.requestId)
    }

    private suspend fun handleCommit(protocol: Protocol, commandType: CommandType) {
        val request = commands.commit.readRequest(protocol)
        log.debug("Request received [command={}, requestId={}]", commandType, request.header.requestId)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val payload = provider.commit(txId)
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            commands.commit.writeResponse(response, protocol)
        }
        log.debug("Response has been sent [command={}, requestId={}]", commandType, request.header.requestId)
    }

    private suspend fun handleFlush(protocol: Protocol, commandType: CommandType) {
        val request = commands.flush.readRequest(protocol)
        log.debug("Request received [command={}, requestId={}]", commandType, request.header.requestId)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val sequenceNumber = request.header.sequenceNumber

            val payload = provider.executeInTransaction(txId, sequenceNumber) { txn ->
                txn.flush()
            }
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            commands.flush.writeResponse(response, protocol)
        }
        log.debug("Response has been sent [command={}, requestId={}]", commandType, request.header.requestId)
    }

    private suspend fun handleCreate(protocol: Protocol, commandType: CommandType) {
        val request = commands.create.readRequest(protocol)
        log.debug("Request received [command={}, requestId={}]", commandType, request.header.requestId)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val sequenceNumber = request.header.sequenceNumber

            val payload = provider.executeInTransaction(txId, sequenceNumber) { txn ->
                txn.create(request.payload!!)
            }
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            commands.create.writeResponse(response, protocol)
        }
        log.debug("Response has been sent [command={}, requestId={}]", commandType, request.header.requestId)
    }

    private suspend fun handleUpdate(protocol: Protocol, commandType: CommandType) {
        val request = commands.update.readRequest(protocol)
        log.debug("Request received [command={}, requestId={}]", commandType, request.header.requestId)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val sequenceNumber = request.header.sequenceNumber

            val payload = provider.executeInTransaction(txId, sequenceNumber) { txn ->
                txn.update(request.payload!!)
            }
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            commands.update.writeResponse(response, protocol)
        }
        log.debug("Response has been sent [command={}, requestId={}]", commandType, request.header.requestId)
    }

    private suspend fun handleDelete(protocol: Protocol, commandType: CommandType) {
        val request = commands.delete.readRequest(protocol)
        log.debug("Request received [command={}, requestId={}]", commandType, request.header.requestId)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val sequenceNumber = request.header.sequenceNumber

            val payload = provider.executeInTransaction(txId, sequenceNumber) { txn ->
                txn.delete(request.payload!!)
            }
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            commands.delete.writeResponse(response, protocol)
        }
        log.debug("Response has been sent [command={}, requestId={}]", commandType, request.header.requestId)
    }

    private suspend fun handleGet(protocol: Protocol, commandType: CommandType) {
        val request = commands.get.readRequest(protocol)
        log.debug("Request received [command={}, requestId={}]", commandType, request.header.requestId)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val sequenceNumber = request.header.sequenceNumber

            val payload = provider.executeInTransaction(txId, sequenceNumber) { txn ->
                txn.get(request.payload!!)
            }
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            commands.get.writeResponse(response, protocol)
        }
        log.debug("Response has been sent [command={}, requestId={}]", commandType, request.header.requestId)
    }

    private suspend fun handleFind(protocol: Protocol, commandType: CommandType) {
        val request = commands.find.readRequest(protocol)
        log.debug("Request received [command={}, requestId={}]", commandType, request.header.requestId)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val sequenceNumber = request.header.sequenceNumber

            val payload = provider.executeInTransaction(txId, sequenceNumber) { txn ->
                txn.find(request.payload!!)
            }
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            commands.find.writeResponse(response, protocol)
        }
        log.debug("Response has been sent [command={}, requestId={}]", commandType, request.header.requestId)
    }

    private suspend fun withErrorHandler(
        request: Request<out RequestPayload>,
        protocol: Protocol,
        action: suspend () -> Unit
    ) {
        try {
            action()
        } catch (e: Exception) {
            request.header.txId?.let { provider.abort(it) }
            when (e) {
                is CommunicationException -> throw e
                else -> {
                    val message = e.message ?: "Cannot handle command for request ${request.header.requestId}"
                    val errorDto = ErrorDto(message)
                    val header = ResponseHeader(request.header.requestId, request.header.txId, true)
                    val response = Response(header, errorDto)
                    protocol.writeErrorResponse(response)
                    log.info("Error occurred during command execution", e)
                }
            }
        }
    }

    private fun getTxId(request: Request<out RequestPayload>, commandType: CommandType): UUID {
        return request.header.txId
            ?: throw IllegalArgumentException(
                "$commandType command must be executed within a transaction [requestId=${request.header.requestId}]"
            )
    }
}