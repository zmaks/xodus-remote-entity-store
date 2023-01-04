package com.zheltoukhov.xres.server.dto

import com.zheltoukhov.xres.server.protocol.ResponsePayload

data class PageDto(
    val total: Long,
    val skip: Int,
    val take: Int,
    val content: List<EntityDto>
) : ResponsePayload
