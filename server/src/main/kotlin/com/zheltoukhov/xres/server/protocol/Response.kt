package com.zheltoukhov.xres.server.protocol

data class Response(
    val requestId: String,
    val txId: String,
    val type: String,
    val payload: ResponsePayload
)
