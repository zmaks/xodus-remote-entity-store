package com.zheltoukhov.xres.protocol.dto

import com.zheltoukhov.xres.protocol.RequestPayload

data class FilterDto(
    val entityType: String,
    val propertyName: String? = null,
    val value: Comparable<*>? = null,
    val skip: Int? = null,
    val take: Int? = null
) : RequestPayload
