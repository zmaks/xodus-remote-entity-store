package com.zheltoukhov.xres.protocol.command

import com.zheltoukhov.xres.protocol.Protocol
import com.zheltoukhov.xres.protocol.dto.BooleanResultDto
import com.zheltoukhov.xres.protocol.dto.EntityDto
import com.zheltoukhov.xres.protocol.dto.TxDto

object CreateCommand : Command<EntityDto, EntityDto>() {

    override suspend fun writeRequestPayload(payload: EntityDto, protocol: Protocol) {
        protocol.writeEntity(payload)
    }

    override suspend fun readResponsePayload(protocol: Protocol): EntityDto {
        return protocol.readEntity()
    }

    override suspend fun writeResponsePayload(payload: EntityDto, protocol: Protocol) {
        protocol.writeEntity(payload)
    }

    override suspend fun readRequestPayload(protocol: Protocol): EntityDto {
        return protocol.readEntity()
    }
}