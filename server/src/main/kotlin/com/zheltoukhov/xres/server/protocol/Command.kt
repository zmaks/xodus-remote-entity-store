package com.zheltoukhov.xres.server.protocol

import com.zheltoukhov.xres.server.dto.*
import kotlin.reflect.KClass


enum class Command(
    val requestPayloadType: KClass<out RequestPayload>,
    val responsePayloadType: KClass<out ResponsePayload>
) {
    BEGIN_TX(TxDto::class, ResponsePayload.EMPTY),
    COMMIT(RequestPayload.EMPTY, BooleanResultDto::class),
    FLUSH(RequestPayload.EMPTY, BooleanResultDto::class),
    ABORT(RequestPayload.EMPTY, ResponsePayload.EMPTY),

    CREATE(EntityDto::class, EntityDto::class),
    UPDATE(EntityDto::class, EntityDto::class),
    DELETE(EntityIdDto::class, BooleanResultDto::class),
    GET(EntityIdDto::class, EntityDto::class),
    FIND(FilterDto::class, PageDto::class);
}