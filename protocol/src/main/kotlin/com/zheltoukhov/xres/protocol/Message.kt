package com.zheltoukhov.xres.protocol

import java.util.UUID

data class Request<T : RequestPayload>(
    val header: RequestHeader,
    val payload: T?
)

data class Response<T : ResponsePayload>(
    val header: ResponseHeader,
    val payload: T? = null
)

data class RequestHeader(
    val requestId: UUID,
    val sequenceNumber: Int,
    val txId: UUID? = null
)

data class ResponseHeader(
    val requestId: UUID,
    val txId: UUID?,
    val isError: Boolean = false
)