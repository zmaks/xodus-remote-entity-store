package com.zheltoukhov.xres.server

import com.zheltoukhov.xres.protocol.*
import com.zheltoukhov.xres.protocol.command.BeginTxCommand
import com.zheltoukhov.xres.protocol.dto.TxDto
import com.zheltoukhov.xres.server.transaction.Transaction
import com.zheltoukhov.xres.server.transaction.TransactionProvider
import com.zheltoukhov.xres.server.transaction.XodusTransaction
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID


@OptIn(ExperimentalCoroutinesApi::class)
class CommandHandlerTest {

    private val protocolMock: Protocol = mockk()
    private val providerMock: TransactionProvider = mockk()
    private val commandHandler = CommandHandler(providerMock)

    @Test
    fun handle_beginTx() = runTest {
        mockkObject(BeginTxCommand)
        val txnMock: Transaction = mockk()
        val txId = UUID.randomUUID()
        val req = Request(RequestHeader(UUID.randomUUID(), 0), TxDto(false))

        every { txnMock.getTransactionId() } returns txId
        coEvery { protocolMock.readCommandType() } returns CommandType.BEGIN_TX
        coEvery { BeginTxCommand.readRequest(any()) } returns req
        coEvery { providerMock.beginTransaction() } returns txnMock

        commandHandler.handle(protocolMock)

        coVerify {
            BeginTxCommand.writeResponse(
                withArg { response ->
                    assertEquals(req.header.requestId, response.header.requestId)
                    assertEquals(txId, response.header.txId)
                    assertNull(response.payload)
                },
                protocolMock
            )
        }

    }
}