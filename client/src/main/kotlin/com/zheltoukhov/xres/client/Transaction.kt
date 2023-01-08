package com.zheltoukhov.xres.client

import com.zheltoukhov.xres.protocol.*
import com.zheltoukhov.xres.protocol.dto.*
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class Transaction(
    private val txId: UUID,
    private val client: StoreClient
) {

    private val lastCommandSequenceNumber = AtomicInteger(0)

    suspend fun create(dto: EntityDto): EntityDto {
        val request = createRequest(dto)
        val response = client.sendCommand<EntityDto, EntityDto>(CommandType.CREATE, request)
        return response.payload!!
    }

    suspend fun update(dto: EntityDto): EntityDto {
        val request = createRequest(dto)
        val response = client.sendCommand<EntityDto, EntityDto>(CommandType.UPDATE, request)
        return response.payload!!
    }

    suspend fun delete(entityId: String): Boolean {
        val request = createRequest(EntityIdDto(entityId))
        val response = client.sendCommand<EntityIdDto, BooleanResultDto>(CommandType.DELETE, request)
        return response.payload!!.result
    }

    suspend fun get(entityId: String): EntityDto {
        val request = createRequest(EntityIdDto(entityId))
        val response = client.sendCommand<EntityIdDto, EntityDto>(CommandType.GET, request)
        return response.payload!!
    }

    suspend fun find(filter: FilterDto): PageDto {
        val request = createRequest(filter)
        val response = client.sendCommand<FilterDto, PageDto>(CommandType.FIND, request)
        return response.payload!!
    }

    suspend fun commit(): Boolean {
        val request = createRequest<EmptyPayload>()
        val response = client.sendCommand<EmptyPayload, BooleanResultDto>(CommandType.COMMIT, request)
        return response.payload!!.result
    }

    suspend fun flush(): Boolean {
        val request = createRequest<EmptyPayload>()
        val response = client.sendCommand<EmptyPayload, BooleanResultDto>(CommandType.FLUSH, request)
        return response.payload!!.result
    }

    suspend fun abort() {
        val request = createRequest<EmptyPayload>()
        client.sendCommand<EmptyPayload, EmptyPayload>(CommandType.ABORT, request)
    }

    private fun <T: RequestPayload> createRequest(payload: T? = null): Request<T> {
        val order = lastCommandSequenceNumber.incrementAndGet()
        val header = RequestHeader(UUID.randomUUID(), order, txId)
        return Request(header, payload)
    }
}