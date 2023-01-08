package com.zheltoukhov.xres.protocol.command

import com.zheltoukhov.xres.protocol.EmptyPayload
import com.zheltoukhov.xres.protocol.Protocol
import com.zheltoukhov.xres.protocol.dto.TxDto

class BeginTxCommand : Command<TxDto, EmptyPayload>() {

    override suspend fun writeRequestPayload(payload: TxDto, protocol: Protocol) {
        protocol.writeTx(payload)
    }

    override suspend fun readRequestPayload(protocol: Protocol): TxDto {
        return protocol.readTx()
    }
}