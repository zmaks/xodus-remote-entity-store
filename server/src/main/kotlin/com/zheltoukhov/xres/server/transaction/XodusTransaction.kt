package com.zheltoukhov.xres.server.transaction

import com.zheltoukhov.xres.protocol.dto.*
import com.zheltoukhov.xres.server.exception.EntityNotFoundException
import jetbrains.exodus.entitystore.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class XodusTransaction(
    store: PersistentEntityStore
) : Transaction {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private val txnId: UUID = UUID.randomUUID()
    private val txn: StoreTransaction = store.beginTransaction()

    companion object {
        const val DEFAULT_LIMIT_PER_PAGE = 100
    }

    override fun getTransactionId(): UUID = txnId

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

    override fun delete(entityIdDto: EntityIdDto): BooleanResultDto {
        val res = getEntityById(entityIdDto.id).delete()
        return BooleanResultDto(res)
    }

    override fun get(entityIdDto: EntityIdDto): EntityDto {
        return getEntityById(entityIdDto.id).toDto()
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

    override fun commit(): BooleanResultDto {
        val result = txn.commit()
        return BooleanResultDto(result)
    }

    override fun flush(): BooleanResultDto {
        val result = txn.flush()
        return BooleanResultDto(result)
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