package com.zheltoukhov.xres.protocol.dto

import com.zheltoukhov.xres.protocol.RequestPayload
import com.zheltoukhov.xres.protocol.ResponsePayload

data class EntityDto(
    val id: String? = null,
    val type: String,
    val properties: Map<String, Comparable<*>>
) : RequestPayload, ResponsePayload