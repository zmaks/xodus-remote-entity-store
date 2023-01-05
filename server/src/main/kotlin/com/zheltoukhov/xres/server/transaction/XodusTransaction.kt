package com.zheltoukhov.xres.server.transaction

import com.zheltoukhov.xres.protocol.dto.EntityDto
import com.zheltoukhov.xres.protocol.dto.FilterDto
import com.zheltoukhov.xres.protocol.dto.PageDto
import com.zheltoukhov.xres.server.exception.EntityNotFoundException
import jetbrains.exodus.entitystore.*
import java.util.*

class XodusTransaction(
    store: PersistentEntityStore
) : Transaction {

    private val txnId: String = UUID.randomUUID().toString()
    private val txn: StoreTransaction = store.beginTransaction()

    companion object {
        const val DEFAULT_LIMIT_PER_PAGE = 100
    }

    override fun getTransactionId(): String = txnId

    override fun create(dto: EntityDto): EntityDto {
        return txn.newEntity(dto.type)
            .setProperties(dto)
            .toDto()
    }

    override fun update(dto: EntityDto): EntityDto {
        val id = dto.id ?: throw IllegalArgumentException("Entity id is null")
        return getEntityById(id)
            .setProperties(dto)
            .toDto()
    }

    override fun delete(entityId: String): Boolean {
        return getEntityById(entityId).delete()
    }

    override fun get(entityId: String): EntityDto {
        return getEntityById(entityId).toDto()
    }

    override fun find(filter: FilterDto): PageDto {
        val entityIterable = if (filter.propertyName != null && filter.value != null) {
            txn.find(filter.entityType, filter.propertyName!!, filter.value!!)
        } else {
            txn.getAll(filter.entityType)
        }
        val total = entityIterable.count()
        val take = filter.take ?: DEFAULT_LIMIT_PER_PAGE
        val skip = filter.skip ?: 0
        val entities = entityIterable
            .take(take)
            .skip(skip)
            .map { entity -> entity.toDto() }

        return PageDto(total, skip, take, entities)
    }

    override fun commit(): Boolean {
        return txn.commit()
    }

    override fun flush(): Boolean {
        return txn.flush()
    }

    override fun abort() {
        txn.abort()
    }

    private fun getEntityById(id: String): Entity {
        try {
            return txn.getEntity(PersistentEntityId.toEntityId(id))
        } catch (e: EntityRemovedInDatabaseException) {
            throw EntityNotFoundException("Entity with id $id not found")
        }
    }

    private fun Entity.toDto(): EntityDto {
        return EntityDto(
            this.toIdString(),
            this.type,
            this.propertyNames.associateWith { name -> this.getProperty(name)!! }
        )
    }

    private fun Entity.setProperties(dto: EntityDto): Entity {
        dto.properties.forEach { (name, value) -> this.setProperty(name, value) }
        return this
    }

}