package com.zheltoukhov.xres.server.dto

import com.zheltoukhov.xres.server.protocol.ResponsePayload

data class BooleanResultDto(
    val result: Boolean
) : ResponsePayload
