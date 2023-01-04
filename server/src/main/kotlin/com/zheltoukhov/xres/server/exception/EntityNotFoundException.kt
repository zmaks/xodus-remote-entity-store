package com.zheltoukhov.xres.server.exception

class EntityNotFoundException : StoreException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}