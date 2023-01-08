package com.zheltoukhov.xres.client

import com.zheltoukhov.xres.client.connection.Connection
import com.zheltoukhov.xres.client.connection.DefaultSocketFactory
import com.zheltoukhov.xres.client.connection.SocketConfig
import com.zheltoukhov.xres.client.connection.SocketFactory
import com.zheltoukhov.xres.protocol.*
import com.zheltoukhov.xres.protocol.command.*
import com.zheltoukhov.xres.protocol.dto.EntityDto
import com.zheltoukhov.xres.protocol.dto.EntityIdDto
import com.zheltoukhov.xres.protocol.dto.FilterDto
import com.zheltoukhov.xres.protocol.dto.TxDto

class StoreClient(
    config: SocketConfig? = null,
    private val socketFactory: SocketFactory = DefaultSocketFactory(config)
) {

    @Suppress("unchecked_cast")
    suspend fun <T : RequestPayload, K : ResponsePayload> sendCommand(
        commandType: CommandType,
        request: Request<T>
    ): Response<K> {
        val connection = Connection(socketFactory)
        val response: Response<out ResponsePayload> = connection.use {
            val protocol = connection.connect()
            protocol.writeCommandType(commandType)
            when (commandType) {
                CommandType.BEGIN_TX -> {
                    BeginTxCommand.writeRequest(request as Request<TxDto>, protocol)
                    BeginTxCommand.readResponse(protocol)
                }
                CommandType.COMMIT -> {
                    CommitCommand.writeRequest(request as Request<EmptyPayload>, protocol)
                    CommitCommand.readResponse(protocol)
                }
                CommandType.FLUSH -> {
                    FlushCommand.writeRequest(request as Request<EmptyPayload>, protocol)
                    FlushCommand.readResponse(protocol)
                }
                CommandType.ABORT -> {
                    AbortCommand.writeRequest(request as Request<EmptyPayload>, protocol)
                    AbortCommand.readResponse(protocol)
                }
                CommandType.CREATE -> {
                    CreateCommand.writeRequest(request as Request<EntityDto>, protocol)
                    CreateCommand.readResponse(protocol)
                }
                CommandType.UPDATE -> {
                    UpdateCommand.writeRequest(request as Request<EntityDto>, protocol)
                    UpdateCommand.readResponse(protocol)
                }
                CommandType.DELETE -> {
                    DeleteCommand.writeRequest(request as Request<EntityIdDto>, protocol)
                    DeleteCommand.readResponse(protocol)
                }
                CommandType.GET -> {
                    GetCommand.writeRequest(request as Request<EntityIdDto>, protocol)
                    GetCommand.readResponse(protocol)
                }
                CommandType.FIND -> {
                    FindCommand.writeRequest(request as Request<FilterDto>, protocol)
                    FindCommand.readResponse(protocol)
                }
            }
        }
        return response as Response<K>
    }

}