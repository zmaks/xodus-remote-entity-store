package com.zheltoukhov.xres.protocol

interface RequestPayload {
    companion object {
        val EMPTY = EmptyPayload::class
    }
}

interface ResponsePayload {
    companion object {
        val EMPTY = EmptyPayload()::class
    }
}

class EmptyPayload : RequestPayload, ResponsePayload