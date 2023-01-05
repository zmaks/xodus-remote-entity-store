package com.zheltoukhov.xres.server.transaction

import com.zheltoukhov.xres.protocol.dto.EntityDto
import com.zheltoukhov.xres.protocol.dto.FilterDto
import com.zheltoukhov.xres.protocol.dto.PageDto

interface Transaction {

    fun getTransactionId(): String

    fun create(dto: EntityDto): EntityDto

    fun update(dto: EntityDto): EntityDto

    fun delete(entityId: String): Boolean

    fun get(entityId: String): EntityDto

    fun find(filter: FilterDto): PageDto

    fun commit(): Boolean

    fun flush(): Boolean

    fun abort()
}