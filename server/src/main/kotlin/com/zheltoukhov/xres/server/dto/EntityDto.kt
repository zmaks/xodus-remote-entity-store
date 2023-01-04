package com.zheltoukhov.xres.server.dto

import com.zheltoukhov.xres.server.protocol.RequestPayload
import com.zheltoukhov.xres.server.protocol.ResponsePayload

data class EntityDto(
    val id: String?,
    val type: String,
    val properties: Map<String, Comparable<Any>>
) : RequestPayload, ResponsePayload