package com.zheltoukhov.xres.server.dto

import com.zheltoukhov.xres.server.protocol.ResponsePayload

data class ErrorDto(
    val code: String,
    val message: String
) : ResponsePayload