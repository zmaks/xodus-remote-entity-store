package com.zheltoukhov.xres.protocol.command

import com.zheltoukhov.xres.protocol.Protocol
import com.zheltoukhov.xres.protocol.dto.BooleanResultDto
import com.zheltoukhov.xres.protocol.dto.EntityIdDto
import com.zheltoukhov.xres.protocol.dto.TxDto

object DeleteCommand : Command<EntityIdDto, BooleanResultDto>() {

    override suspend fun writeRequestPayload(payload: EntityIdDto, protocol: Protocol) {
        protocol.writeEntityId(payload.id)
    }

    override suspend fun readResponsePayload(protocol: Protocol): BooleanResultDto {
        return protocol.readBooleanResult()
    }

    override suspend fun writeResponsePayload(payload: BooleanResultDto, protocol: Protocol) {
        protocol.writeBooleanResult(payload)
    }

    override suspend fun readRequestPayload(protocol: Protocol): EntityIdDto {
        val entityId = protocol.readEntityId() ?: throw IllegalArgumentException("Entity ID is null for DeleteCommand")
        return EntityIdDto(entityId)
    }
}