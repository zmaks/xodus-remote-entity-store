package com.zheltoukhov.xres.server.dto

import com.zheltoukhov.xres.server.protocol.RequestPayload

data class EntityIdDto(
    val id: String
) : RequestPayload