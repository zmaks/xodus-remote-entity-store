package com.zheltoukhov.xres.server.dto

import com.zheltoukhov.xres.server.protocol.RequestPayload

data class FilterDto(
    val entityType: String,
    val propertyName: String?,
    val value: Comparable<Any>?,
    val skip: Int?,
    val take: Int?
) : RequestPayload
