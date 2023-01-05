package com.zheltoukhov.xres.protocol.dto

import com.zheltoukhov.xres.protocol.ResponsePayload

data class BooleanResultDto(
    val result: Boolean
) : ResponsePayload
