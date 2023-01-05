package com.zheltoukhov.xres.protocol.dto

import com.zheltoukhov.xres.protocol.ResponsePayload

data class PageDto(
    val total: Long,
    val skip: Int,
    val take: Int,
    val content: List<EntityDto>
) : ResponsePayload
