package com.zheltoukhov.xres.client

import com.zheltoukhov.xres.client.connection.Connection
import com.zheltoukhov.xres.client.connection.DefaultSocketFactory
import com.zheltoukhov.xres.client.connection.SocketConfig
import com.zheltoukhov.xres.client.connection.SocketFactory
import com.zheltoukhov.xres.protocol.*
import com.zheltoukhov.xres.protocol.command.Commands
import com.zheltoukhov.xres.protocol.dto.EntityDto
import com.zheltoukhov.xres.protocol.dto.EntityIdDto
import com.zheltoukhov.xres.protocol.dto.FilterDto
import com.zheltoukhov.xres.protocol.dto.TxDto

class StoreClient(
    config: SocketConfig? = null,
    private val socketFactory: SocketFactory = DefaultSocketFactory(config),
    private val commands: Commands = Commands()
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
                    commands.beginTx.writeRequest(request as Request<TxDto>, protocol)
                    commands.beginTx.readResponse(protocol)
                }
                CommandType.COMMIT -> {
                    commands.commit.writeRequest(request as Request<EmptyPayload>, protocol)
                    commands.commit.readResponse(protocol)
                }
                CommandType.FLUSH -> {
                    commands.flush.writeRequest(request as Request<EmptyPayload>, protocol)
                    commands.flush.readResponse(protocol)
                }
                CommandType.ABORT -> {
                    commands.abort.writeRequest(request as Request<EmptyPayload>, protocol)
                    commands.abort.readResponse(protocol)
                }
                CommandType.CREATE -> {
                    commands.create.writeRequest(request as Request<EntityDto>, protocol)
                    commands.create.readResponse(protocol)
                }
                CommandType.UPDATE -> {
                    commands.update.writeRequest(request as Request<EntityDto>, protocol)
                    commands.update.readResponse(protocol)
                }
                CommandType.DELETE -> {
                    commands.delete.writeRequest(request as Request<EntityIdDto>, protocol)
                    commands.delete.readResponse(protocol)
                }
                CommandType.GET -> {
                    commands.get.writeRequest(request as Request<EntityIdDto>, protocol)
                    commands.get.readResponse(protocol)
                }
                CommandType.FIND -> {
                    commands.find.writeRequest(request as Request<FilterDto>, protocol)
                    commands.find.readResponse(protocol)
                }
            }
        }
        return response as Response<K>
    }

}