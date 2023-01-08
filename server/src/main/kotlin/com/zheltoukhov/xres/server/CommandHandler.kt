package com.zheltoukhov.xres.server

import com.zheltoukhov.xres.protocol.*
import com.zheltoukhov.xres.protocol.command.*
import com.zheltoukhov.xres.protocol.dto.ErrorDto
import com.zheltoukhov.xres.protocol.exception.CommandErrorException
import com.zheltoukhov.xres.protocol.exception.CommunicationException
import com.zheltoukhov.xres.server.exception.StoreException
import com.zheltoukhov.xres.server.transaction.TransactionProvider
import io.ktor.utils.io.*
import java.util.UUID

class CommandHandler(
    private val provider: TransactionProvider
) {

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
        val request = BeginTxCommand.readRequest(protocol)
        withErrorHandler(request, protocol) {
            if (request.header.txId != null) throw IllegalArgumentException(
                "$commandType cannot be executed within existing transaction ${request.header.txId} " +
                        "[requestId=${request.header.requestId}]"
            )
            val txn = provider.beginTransaction()
            val response = Response<EmptyPayload>(ResponseHeader(request.header.requestId, txn.getTransactionId()))
            BeginTxCommand.writeResponse(response, protocol)
        }
    }

    private suspend fun handleAbort(protocol: Protocol, commandType: CommandType) {
        val request = AbortCommand.readRequest(protocol)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            provider.abort(txId)
            val response = Response<EmptyPayload>(ResponseHeader(request.header.requestId, txId))
            AbortCommand.writeResponse(response, protocol)
        }
    }

    private suspend fun handleCommit(protocol: Protocol, commandType: CommandType) {
        val request = CommitCommand.readRequest(protocol)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val payload = provider.commit(txId)
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            CommitCommand.writeResponse(response, protocol)
        }
    }

    private suspend fun handleFlush(protocol: Protocol, commandType: CommandType) {
        val request = FlushCommand.readRequest(protocol)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val sequenceNumber = request.header.sequenceNumber

            val payload = provider.executeInTransaction(txId, sequenceNumber) { txn ->
                txn.flush()
            }
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            FlushCommand.writeResponse(response, protocol)
        }
    }

    private suspend fun handleCreate(protocol: Protocol, commandType: CommandType) {
        val request = CreateCommand.readRequest(protocol)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val sequenceNumber = request.header.sequenceNumber

            val payload = provider.executeInTransaction(txId, sequenceNumber) { txn ->
                txn.create(request.payload!!)
            }
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            CreateCommand.writeResponse(response, protocol)
        }
    }

    private suspend fun handleUpdate(protocol: Protocol, commandType: CommandType) {
        val request = UpdateCommand.readRequest(protocol)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val sequenceNumber = request.header.sequenceNumber

            val payload = provider.executeInTransaction(txId, sequenceNumber) { txn ->
                txn.update(request.payload!!)
            }
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            UpdateCommand.writeResponse(response, protocol)
        }
    }

    private suspend fun handleDelete(protocol: Protocol, commandType: CommandType) {
        val request = DeleteCommand.readRequest(protocol)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val sequenceNumber = request.header.sequenceNumber

            val payload = provider.executeInTransaction(txId, sequenceNumber) { txn ->
                txn.delete(request.payload!!)
            }
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            DeleteCommand.writeResponse(response, protocol)
        }
    }

    private suspend fun handleGet(protocol: Protocol, commandType: CommandType) {
        val request = GetCommand.readRequest(protocol)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val sequenceNumber = request.header.sequenceNumber

            val payload = provider.executeInTransaction(txId, sequenceNumber) { txn ->
                txn.get(request.payload!!)
            }
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            GetCommand.writeResponse(response, protocol)
        }
    }

    private suspend fun handleFind(protocol: Protocol, commandType: CommandType) {
        val request = FindCommand.readRequest(protocol)
        withErrorHandler(request, protocol) {
            val txId = getTxId(request, commandType)
            val sequenceNumber = request.header.sequenceNumber

            val payload = provider.executeInTransaction(txId, sequenceNumber) { txn ->
                txn.find(request.payload!!)
            }
            val response = Response(ResponseHeader(request.header.requestId, txId), payload)
            FindCommand.writeResponse(response, protocol)
        }
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