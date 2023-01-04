package com.zheltoukhov.xres.server.protocol

data class Request(
    val id: String,
    val command: String,
    val txId: String?,
    val payload: RequestPayload
)