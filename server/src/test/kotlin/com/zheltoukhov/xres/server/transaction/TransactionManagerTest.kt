package com.zheltoukhov.xres.server.transaction

import com.zheltoukhov.xres.protocol.EmptyPayload
import com.zheltoukhov.xres.server.exception.StoreException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.exodus.entitystore.StoreTransaction
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionManagerTest {

    @Test
    fun testConcurrentTransactionsInReverseOrder() = runTest {
        val storeMock: PersistentEntityStore = mockk {
            every { beginTransaction() } returns mockk()
        }
        val txManager = TransactionManager(storeMock)
        val txn = txManager.beginTransaction()
        val counter = AtomicInteger()

        withContext(Dispatchers.Default)  {
            for (i in 100 downTo 1) {
                launch {
                    txManager.executeInTransaction(txn.getTransactionId(), i) {
                        counter.incrementAndGet()
                        EmptyPayload()
                    }
                }
            }
        }
        assertEquals(100, counter.get())
    }

    @Test
    fun testUnorderedTransaction() = runTest {
        val txMock: StoreTransaction = mockk {
            every { abort() } returns Unit
        }
        val storeMock: PersistentEntityStore = mockk {
            every { beginTransaction() } returns txMock
        }
        val txManager = TransactionManager(storeMock)
        val txn = txManager.beginTransaction()

        val ex = assertThrows<StoreException> {
            txManager.executeInTransaction(txn.getTransactionId(), 10) { EmptyPayload() }
        }
        assertEquals("Sequence number of transaction ${txn.getTransactionId()} is further than previous" +
                " executed operation.The transaction has been aborted.", ex.message)
    }

    @Test
    fun testAbort() = runTest {
        val txMock: StoreTransaction = mockk {
            every { abort() } returns Unit
        }
        val storeMock: PersistentEntityStore = mockk {
            every { beginTransaction() } returns txMock
        }

        val txManager = TransactionManager(storeMock)
        val txn = txManager.beginTransaction()
        txManager.abort(txn.getTransactionId())

        verify { txMock.abort() }
        val ex = assertThrows<StoreException> { txManager.commit(txn.getTransactionId()) }
        assertEquals("Unable to commit. Transaction ${txn.getTransactionId()} not found", ex.message)
    }

    @Test
    fun testCommit() = runTest {
        val txMock: StoreTransaction = mockk {
            every { commit() } returns true
        }
        val storeMock: PersistentEntityStore = mockk {
            every { beginTransaction() } returns txMock
        }

        val txManager = TransactionManager(storeMock)
        val txn = txManager.beginTransaction()
        txManager.commit(txn.getTransactionId())

        verify { txMock.commit() }
        val ex = assertThrows<StoreException> { txManager.commit(txn.getTransactionId()) }
        assertEquals("Unable to commit. Transaction ${txn.getTransactionId()} not found", ex.message)
    }
}