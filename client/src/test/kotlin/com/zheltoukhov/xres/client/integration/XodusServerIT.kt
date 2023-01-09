package com.zheltoukhov.xres.client.integration

import com.zheltoukhov.xres.client.EntityStore
import com.zheltoukhov.xres.client.StoreClient
import com.zheltoukhov.xres.client.connection.SocketConfig
import com.zheltoukhov.xres.protocol.dto.EntityDto
import com.zheltoukhov.xres.protocol.dto.FilterDto
import com.zheltoukhov.xres.protocol.exception.CommandErrorException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class XodusServerIT : FunSpec({

    val container = install(TestContainerExtension("zumaxim/xres")) {
        startupAttempts = 1
        withExposedPorts(9042)
    }

    val conf = SocketConfig(container.host, container.getMappedPort(9042))
    val entityStore = EntityStore(StoreClient(conf))

    test("crud methods") {
        val tx = entityStore.beginTransaction()
        val entity1 = tx.create(EntityDto(type = "CRUD", properties = mapOf("foo" to "bar")))
        val entity2 = tx.create(EntityDto(type = "CRUD", properties = mapOf("two" to 2)))

        entity1.id shouldNotBe null
        entity2.id shouldNotBe null

        val updatedEntity2 = tx.update(
            EntityDto(entity2.id, "CRUD", entity2.properties + mapOf("some" to "new"))
        )

        updatedEntity2.properties["some"] shouldBe "new"
        updatedEntity2.properties["two"] shouldBe 2

        tx.delete(entity2.id!!) shouldBe true

        val entitiesPage = tx.find(FilterDto("CRUD"))

        entitiesPage.content.size shouldBe 1
        entitiesPage.content.first().id shouldBe entity1.id

        tx.get(entity1.id!!).id shouldBe entity1.id

        tx.commit() shouldBe true
    }

    test("commit transaction") {
        val tx = entityStore.beginTransaction()
        val entity = tx.create(EntityDto(type = "COMMIT", properties = mapOf("foo" to "bar")))
        tx.commit()

        val e = shouldThrow<CommandErrorException> {
            tx.commit()
        }
        e.message shouldContain "not found"

        val tx2 = entityStore.beginTransaction()
        tx2.get(entity.id!!).id shouldBe entity.id
        tx2.commit()
    }

    test("abort transaction") {
        val tx = entityStore.beginTransaction()
        val entity = tx.create(EntityDto(type = "ABORT", properties = mapOf("foo" to "bar")))
        tx.abort()

        val e = shouldThrow<CommandErrorException> {
            tx.get(entity.id!!)
        }
        e.message shouldContain "not found"

        val tx2 = entityStore.beginTransaction()
        shouldThrowMessage("Entity with id ${entity.id} not found") {
            tx2.get(entity.id!!)
        }
        tx2.commit()
    }

    test("concurrent transactions isolation") {
        val tx1 = entityStore.beginTransaction()
        val tx2 = entityStore.beginTransaction()

        val entity = tx2.create(EntityDto(type = "ISO", properties = mapOf("foo" to "bar")))

        shouldThrowMessage("Entity with id ${entity.id} not found") {
            tx1.get(entity.id!!)
        }

        tx2.commit()

        shouldThrowMessage("Entity with id ${entity.id} not found") {
            tx1.get(entity.id!!)
        }
    }

    test("parallel operations") {
        val tx = entityStore.beginTransaction()
        val entity = tx.create(EntityDto(type = "PARALLEL", properties = mapOf("val" to 0)))

        runBlocking {
            repeat(100) {
                launch {
                    val updated = tx.update(EntityDto(entity.id, entity.type, mapOf("val" to it)))
                    updated.properties["val"] shouldBe it
                }
            }
        }
        tx.commit()
    }
})