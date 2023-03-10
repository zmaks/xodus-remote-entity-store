package com.zheltoukhov.xres.server.exception

open class StoreException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}