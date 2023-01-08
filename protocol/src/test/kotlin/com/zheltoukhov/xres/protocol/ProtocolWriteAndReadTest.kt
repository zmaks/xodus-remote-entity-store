package com.zheltoukhov.xres.protocol

import com.zheltoukhov.xres.protocol.dto.*
import io.ktor.utils.io.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProtocolWriteAndReadTest {

    private lateinit var protocol: Protocol
    private lateinit var channel: ByteChannel

    @BeforeEach
    fun setUp() {
        channel = ByteChannel(false)
        protocol = Protocol(channel, channel)
    }

    @AfterEach
    fun reset() {
        channel.close()
    }

    @Test
    fun testCommandType() = runTest {
        val commandType = CommandType.BEGIN_TX
        protocol.writeCommandType(commandType)
        protocol.flush()
        assertEquals(commandType, protocol.readCommandType())
    }

    @Test
    fun testUuid() = runTest {
        val uuid = UUID.randomUUID()
        protocol.writeUUID(uuid)
        protocol.flush()
        assertEquals(uuid.toString(), protocol.readUUID().toString())
    }

    @Test
    fun testRequestHeader() = runTest {
        val header = RequestHeader(UUID.randomUUID(), 42, UUID.randomUUID())
        protocol.writeRequestHeader(header)
        protocol.flush()
        assertEquals(header, protocol.readRequestHeader())
    }

    @Test
    fun testRequestHeader_nullTxId() = runTest {
        val header = RequestHeader(UUID.randomUUID(), 42, null)
        protocol.writeRequestHeader(header)
        protocol.flush()
        assertEquals(header, protocol.readRequestHeader())
    }

    @Test
    fun testResponseHeader() = runTest {
        val header = ResponseHeader(UUID.randomUUID(), UUID.randomUUID())
        protocol.writeResponseHeader(header)
        protocol.flush()
        assertEquals(header, protocol.readResponseHeader())
    }

    @Test
    fun testString() = runTest {
        val str = "some-random-string"
        protocol.writeString(str)
        protocol.flush()
        assertEquals(str, protocol.readString())
    }

    @Test
    fun testEncodedString() = runTest {
        val str = """
            {
                "field1": "\sd%!@#$()*&^%>~+-/|><,'`1234567890"
            }
        """.trimIndent()
        protocol.writeStringEncoded(str)
        protocol.flush()
        assertEquals(str, protocol.readStringDecoded())
    }

    @Test
    fun testShortString() = runTest {
        var str = ""
        repeat(Byte.MAX_VALUE.toInt()) { str+= "a" }
        protocol.writeShortString(str)
        protocol.flush()
        assertEquals(str, protocol.readShortString())
    }

    @Test
    fun testShortString_failed_tooLongString() = runTest {
        var str = ""
        repeat(Byte.MAX_VALUE.toInt() + 1) { str+= "a" }
        assertThrows<IllegalArgumentException> { protocol.writeShortString(str) }
    }

    @Test
    fun testEntityDto() = runTest {
        val entityDto = EntityDto(
            "entityId",
            "EntityType",
            mapOf(
                "boolean" to true,
                "byte" to Byte.MAX_VALUE,
                "short" to Short.MAX_VALUE,
                "int" to Int.MAX_VALUE,
                "long" to Long.MAX_VALUE,
                "float" to 1.4242f,
                "double" to 21.21212121,
                "string" to "{\"a\": \"1\",\n\"b\": \"2\"}"
            )
        )
        protocol.writeEntity(entityDto)
        protocol.flush()
        assertEquals(entityDto, protocol.readEntity())
    }

    @Test
    fun testPageDto() = runTest {
        val entityDto1 = EntityDto(
            null,
            "EntityType",
            mapOf()
        )
        val entityDto2 = EntityDto(
            "id",
            "EntityType",
            mapOf("a" to "b")
        )
        val page = PageDto(2, 0, 10, listOf(entityDto1, entityDto2))
        protocol.writePage(page)
        protocol.flush()
        assertEquals(page, protocol.readPage())
    }

    @Test
    fun testFilterDto() = runTest {
        val filterDto1 = FilterDto("EntityType")
        val filterDto2 = FilterDto("EntityType", propertyName = "prop", value = "filter")
        val filterDto3 = FilterDto("EntityType", propertyName = "prop", value = "filter", skip = 1, take = 5)
        protocol.writeFilter(filterDto1)
        protocol.writeFilter(filterDto2)
        protocol.writeFilter(filterDto3)
        protocol.flush()
        assertEquals(filterDto1, protocol.readFilter())
        assertEquals(filterDto2, protocol.readFilter())
        assertEquals(filterDto3, protocol.readFilter())
    }

    @Test
    fun testTx() = runTest {
        val tx = TxDto(false)
        protocol.writeTx(tx)
        protocol.flush()
        assertEquals(tx, protocol.readTx())
    }

    @Test
    fun testError() = runTest {
        val error = ErrorDto("reason")
        protocol.writeError(error)
        protocol.flush()
        assertEquals(error, protocol.readError())
    }

    @Test
    fun testBooleanResultDto() = runTest {
        val booleanResult = BooleanResultDto(true)
        protocol.writeBooleanResult(booleanResult)
        protocol.flush()
        assertEquals(booleanResult, protocol.readBooleanResult())
    }

}