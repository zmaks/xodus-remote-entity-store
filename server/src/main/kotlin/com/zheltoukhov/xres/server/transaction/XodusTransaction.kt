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
        val entity = txn.newEntity(dto.type)
            .setProperties(dto)
            .toDto()
        log.debug("[tx={}] created new entity with id {}", txnId, entity.id)
        return entity
    }

    override fun update(dto: EntityDto): EntityDto {
        val id = dto.id ?: throw IllegalArgumentException("Entity id is null")
        val entity =  getEntityById(id)
            .setProperties(dto)
            .toDto()
        log.debug("[tx={}] updated entity with id {}", txnId, entity.id)
        return entity
    }

    override fun delete(entityIdDto: EntityIdDto): BooleanResultDto {
        val res = getEntityById(entityIdDto.id).delete()
        log.debug("[tx={}] deleted entity with id {}", txnId, entityIdDto.id)
        return BooleanResultDto(res)
    }

    override fun get(entityIdDto: EntityIdDto): EntityDto {
        log.debug("[tx={}] get entity by id {}", txnId, entityIdDto.id)
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

        log.debug("[tx={}] found {} entities by filter {}", txnId, total, filter)
        return PageDto(total, skip, take, entities)
    }

    override fun commit(): BooleanResultDto {
        val result = txn.commit()
        log.debug("Transaction {} has been committed", txnId)
        return BooleanResultDto(result)
    }

    override fun flush(): BooleanResultDto {
        val result = txn.flush()
        log.debug("Transaction {} has been flushed", txnId)
        return BooleanResultDto(result)
    }

    override fun abort() {
        txn.abort()
        log.debug("Transaction {} has been aborted", txnId)
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