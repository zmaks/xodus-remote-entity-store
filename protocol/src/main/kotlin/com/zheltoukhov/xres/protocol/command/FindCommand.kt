package com.zheltoukhov.xres.protocol.command

import com.zheltoukhov.xres.protocol.Protocol
import com.zheltoukhov.xres.protocol.dto.BooleanResultDto
import com.zheltoukhov.xres.protocol.dto.FilterDto
import com.zheltoukhov.xres.protocol.dto.PageDto
import com.zheltoukhov.xres.protocol.dto.TxDto

object FindCommand : Command<FilterDto, PageDto>() {

    override suspend fun writeRequestPayload(payload: FilterDto, protocol: Protocol) {
        protocol.writeFilter(payload)
    }

    override suspend fun readResponsePayload(protocol: Protocol): PageDto {
        return protocol.readPage()
    }

    override suspend fun writeResponsePayload(payload: PageDto, protocol: Protocol) {
        protocol.writePage(payload)
    }

    override suspend fun readRequestPayload(protocol: Protocol): FilterDto {
        return protocol.readFilter()
    }
}