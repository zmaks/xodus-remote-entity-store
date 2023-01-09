package com.zheltoukhov.xres.client

import com.zheltoukhov.xres.client.exception.StoreException
import com.zheltoukhov.xres.protocol.CommandType
import com.zheltoukhov.xres.protocol.EmptyPayload
import com.zheltoukhov.xres.protocol.Request
import com.zheltoukhov.xres.protocol.RequestHeader
import com.zheltoukhov.xres.protocol.dto.TxDto
import java.util.*

class EntityStore(
    private val client: StoreClient
) {

    /**
     * Runs a transaction on the server and return the transaction object
     * that allows to run actions within the transaction
     *
     * @return Transaction
     */
    suspend fun beginTransaction(): Transaction {
        val txDto = TxDto(false) //todo implement readOnly transactions
        val request = Request(RequestHeader(UUID.randomUUID(), 0), txDto)
        val response = client.sendCommand<TxDto, EmptyPayload>(CommandType.BEGIN_TX, request)
        val txId = response.header.txId
            ?: throw StoreException("No txId provided in the response [requestId=${request.header.requestId}]")
        return Transaction(txId, client)
    }
}