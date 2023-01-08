package com.zheltoukhov.xres.server.transaction

import com.zheltoukhov.xres.protocol.dto.*
import java.util.UUID

interface Transaction {

    fun getTransactionId(): UUID

    fun create(dto: EntityDto): EntityDto

    fun update(dto: EntityDto): EntityDto

    fun delete(entityIdDto: EntityIdDto): BooleanResultDto

    fun get(entityIdDto: EntityIdDto): EntityDto

    fun find(filter: FilterDto): PageDto

    fun commit(): BooleanResultDto

    fun flush(): BooleanResultDto

    fun abort()
}