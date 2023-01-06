package com.zheltoukhov.xres.protocol

import java.util.UUID

data class Request<T : RequestPayload>(
    val header: Header,
    val payload: T?
)

data class Response<T : ResponsePayload>(
    val header: Header,
    val payload: T?
)

data class Header(
    val requestId: UUID,
    val txId: UUID?
)