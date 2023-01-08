package com.zheltoukhov.xres.client.exception

class StoreException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}