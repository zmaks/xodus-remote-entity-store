package com.zheltoukhov.xres.protocol.command

import com.zheltoukhov.xres.protocol.Protocol
import com.zheltoukhov.xres.protocol.dto.BooleanResultDto
import com.zheltoukhov.xres.protocol.dto.TxDto

object BeginTxCommand : Command<TxDto, BooleanResultDto>() {

    override suspend fun writeRequestPayload(payload: TxDto, protocol: Protocol) {
        protocol.writeTx(payload)
    }

    override suspend fun readResponsePayload(protocol: Protocol): BooleanResultDto {
        return protocol.readBooleanResult()
    }
}