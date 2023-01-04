package com.zheltoukhov.xres.server.exception

sealed class StoreException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}