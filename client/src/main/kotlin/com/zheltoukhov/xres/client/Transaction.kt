package com.zheltoukhov.xres.client

import com.zheltoukhov.xres.protocol.*
import com.zheltoukhov.xres.protocol.dto.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class Transaction(
    private val txId: UUID,
    private val client: StoreClient
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private val lastCommandSequenceNumber = AtomicInteger(0)

    suspend fun create(dto: EntityDto): EntityDto {
        val request = createRequest(dto)
        log.debug("[tx={} seqNum={}] creating new entity with type {}", txId, request.header.sequenceNumber, dto.type)
        val response = client.sendCommand<EntityDto, EntityDto>(CommandType.CREATE, request)
        return response.payload!!
    }

    suspend fun update(dto: EntityDto): EntityDto {
        check(dto.id != null) {
            "Entity with null id cannot be updated"
        }
        val request = createRequest(dto)
        log.debug("[tx={} seqNum={}] updating entity with id {}", txId, request.header.sequenceNumber, dto.id)
        val response = client.sendCommand<EntityDto, EntityDto>(CommandType.UPDATE, request)
        return response.payload!!
    }

    suspend fun delete(entityId: String): Boolean {
        val request = createRequest(EntityIdDto(entityId))
        log.debug("[tx={} seqNum={}] deleting entity with id {}", txId, request.header.sequenceNumber, entityId)
        val response = client.sendCommand<EntityIdDto, BooleanResultDto>(CommandType.DELETE, request)
        return response.payload!!.result
    }

    suspend fun get(entityId: String): EntityDto {
        val request = createRequest(EntityIdDto(entityId))
        log.debug("[tx={} seqNum={}] get entity by id {}", txId, request.header.sequenceNumber, entityId)
        val response = client.sendCommand<EntityIdDto, EntityDto>(CommandType.GET, request)
        return response.payload!!
    }

    suspend fun find(filter: FilterDto): PageDto {
        val request = createRequest(filter)
        log.debug("[tx={} seqNum={}] find entities by filter {}", txId, request.header.sequenceNumber, filter)
        val response = client.sendCommand<FilterDto, PageDto>(CommandType.FIND, request)
        return response.payload!!
    }

    suspend fun commit(): Boolean {
        val request = createRequest<EmptyPayload>()
        log.debug("[tx={} seqNum={}] committing transaction", txId, request.header.sequenceNumber)
        val response = client.sendCommand<EmptyPayload, BooleanResultDto>(CommandType.COMMIT, request)
        return response.payload!!.result
    }

    suspend fun flush(): Boolean {
        val request = createRequest<EmptyPayload>()
        log.debug("[tx={} seqNum={}] committing transaction", txId, request.header.sequenceNumber)
        val response = client.sendCommand<EmptyPayload, BooleanResultDto>(CommandType.FLUSH, request)
        return response.payload!!.result
    }

    suspend fun abort() {
        val request = createRequest<EmptyPayload>()
        log.debug("[tx={} seqNum={}] aborting transaction", txId, request.header.sequenceNumber)
        client.sendCommand<EmptyPayload, EmptyPayload>(CommandType.ABORT, request)
    }

    private fun <T: RequestPayload> createRequest(payload: T? = null): Request<T> {
        val order = lastCommandSequenceNumber.incrementAndGet()
        val header = RequestHeader(UUID.randomUUID(), order, txId)
        return Request(header, payload)
    }
}