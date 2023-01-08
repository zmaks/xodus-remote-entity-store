package com.zheltoukhov.xres.server.transaction

import com.zheltoukhov.xres.protocol.ResponsePayload
import com.zheltoukhov.xres.protocol.dto.*
import com.zheltoukhov.xres.server.exception.StoreException
import jetbrains.exodus.entitystore.PersistentEntityStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class TransactionProvider(
    private val store: PersistentEntityStore,
    private val txTimeoutMs: Long = 1 * 60 * 60 * 1000,
    private val maxSequenceRetries: Int = 500,
    private val sequenceWaitMs: Long = 10
) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private val contextStore = ConcurrentHashMap<UUID, TransactionContext>()
    private val txQueue = ConcurrentLinkedQueue<TxIdTime>()

    fun beginTransaction(): Transaction {
        releaseOutdatedFromQueue()

        val txn = XodusTransaction(store)
        val txId = txn.getTransactionId()
        val context = TransactionContext(txn, Mutex(), AtomicInteger())
        contextStore[txId] = context
        register(txId)

        log.debug("Transaction $txId has been registered")
        return txn
    }

    fun abort(txId: UUID) {
        removeTransactionContext(txId)?.transaction?.abort()
            ?: throw StoreException("Unable to abort. Transaction $txId not found")
    }

    fun commit(txId: UUID): BooleanResultDto {
        return removeTransactionContext(txId)?.transaction?.commit()
            ?: throw StoreException("Unable to commit. Transaction $txId not found")
    }

    suspend fun <T : ResponsePayload> executeInTransaction(
        txId: UUID,
        sequenceNumber: Int,
        transactionAction: (Transaction) -> T?
    ): T? {
        val context = getTransactionContext(txId)
        return try {
            context.mutex.lock()
            if (sequenceNumber != context.lastSequenceNumber.get() + 1) {
                context.mutex.unlock()
                waitForSequence(txId, sequenceNumber, context.lastSequenceNumber.get())
                context.mutex.lock()
            }
            val result = transactionAction(context.transaction)
            context.lastSequenceNumber.incrementAndGet()
            result
        } finally {
            context.mutex.unlock()
        }
    }

    private suspend fun waitForSequence(txId: UUID, currentOrder: Int, lastOrder: Int) {
        var retries = 0
        while (currentOrder != lastOrder + 1) {
            if (++retries >= maxSequenceRetries) {
                abort(txId)
                throw StoreException("Tr")
            }
            delay(sequenceWaitMs)
        }
    }

    private fun getTransactionContext(txId: UUID): TransactionContext {
        return contextStore[txId] ?: throw IllegalArgumentException("Transaction $txId not found")
    }

    private fun removeTransactionContext(txId: UUID): TransactionContext? {
        txQueue.removeIf { it.id == txId }
        return contextStore.remove(txId)
    }

    private fun register(txId: UUID) = txQueue.add(TxIdTime(txId, System.currentTimeMillis()))

    private fun releaseOutdatedFromQueue() {
        val now = System.currentTimeMillis()
        while (txQueue.peek()?.let { now - it.timestamp > txTimeoutMs } == true) {
            val txId = txQueue.poll()!!.id
            abort(txId)
        }
    }

    data class TxIdTime(
        val id: UUID,
        val timestamp: Long
    )

    data class TransactionContext(
        val transaction: Transaction,
        val mutex: Mutex,
        val lastSequenceNumber: AtomicInteger
    )
}