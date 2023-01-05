package com.zheltoukhov.xres.protocol.dto

import com.zheltoukhov.xres.protocol.ResponsePayload

data class ErrorDto(
    val message: String
) : ResponsePayload