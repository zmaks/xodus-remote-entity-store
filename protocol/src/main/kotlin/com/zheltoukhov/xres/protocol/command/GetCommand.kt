package com.zheltoukhov.xres.protocol.command

import com.zheltoukhov.xres.protocol.Protocol
import com.zheltoukhov.xres.protocol.dto.BooleanResultDto
import com.zheltoukhov.xres.protocol.dto.EntityDto
import com.zheltoukhov.xres.protocol.dto.EntityIdDto
import com.zheltoukhov.xres.protocol.dto.TxDto

object GetCommand : Command<EntityIdDto, EntityDto>() {

    override suspend fun writeRequestPayload(payload: EntityIdDto, protocol: Protocol) {
        protocol.writeEntityId(payload.id)
    }

    override suspend fun readResponsePayload(protocol: Protocol): EntityDto {
        return protocol.readEntity()
    }

    override suspend fun writeResponsePayload(payload: EntityDto, protocol: Protocol) {
        protocol.writeEntity(payload)
    }

    override suspend fun readRequestPayload(protocol: Protocol): EntityIdDto {
        val entityId = protocol.readEntityId() ?: throw IllegalArgumentException("Entity ID is null for GetCommand")
        return EntityIdDto(entityId)
    }
}