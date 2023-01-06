package com.zheltoukhov.xres.protocol.command

import com.zheltoukhov.xres.protocol.EmptyPayload
import com.zheltoukhov.xres.protocol.Protocol
import com.zheltoukhov.xres.protocol.dto.BooleanResultDto
import com.zheltoukhov.xres.protocol.dto.TxDto

object FlushCommand : Command<EmptyPayload, BooleanResultDto>() {

    override suspend fun readResponsePayload(protocol: Protocol): BooleanResultDto {
        return protocol.readBooleanResult()
    }

    override suspend fun writeResponsePayload(payload: BooleanResultDto, protocol: Protocol) {
        protocol.writeBooleanResult(payload)
    }
}