package com.zheltoukhov.xres.protocol

import com.zheltoukhov.xres.protocol.dto.*
import com.zheltoukhov.xres.protocol.exception.CommunicationException
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import java.nio.ByteBuffer
import java.util.Base64
import java.util.UUID

class Protocol(
    private val writeChannel: ByteWriteChannel,
    private val readChannel: ByteReadChannel
) {

    enum class PropertyType(val code: Byte) {
        BOOLEAN(1),
        BYTE(2),
        SHORT(3),
        INT(4),
        LONG(5),
        FLOAT(6),
        DOUBLE(7),
        STRING(8);
    }

    private val errorCode: Byte = -1

    fun flush() = writeChannel.flush()

    suspend fun writeCommandType(command: CommandType) {
        writeChannel.writeByte(command.code)
    }

    suspend fun readCommandType(): CommandType {
        val code = readChannel.readByte()
        if (code == errorCode) {
            val error = readError()
            throw CommunicationException(error.message)
        }
        return CommandType.fromCode(code) ?: throw IllegalArgumentException("[val=$code] Unknown command code")
    }

    suspend fun writeError(error: ErrorDto) {
        writeChannel.writeByte(errorCode)
        writeStringEncoded(error.message)
    }

    suspend fun readError(): ErrorDto {
        val message = readStringDecoded()
        return ErrorDto(message)
    }

    suspend fun readUUID(): UUID {
        val buffer = ByteBuffer.wrap(readChannel.readPacket(16).readBytes())
        val mostSignificantBits = buffer.long
        val leastSignificantBits = buffer.long
        return UUID(mostSignificantBits, leastSignificantBits)
    }

    suspend fun writeUUID(uuid: UUID) {
        writeChannel.writeLong(uuid.mostSignificantBits)
        writeChannel.writeLong(uuid.leastSignificantBits)
    }

    suspend fun writeString(str: String) {
        val stringBytes = str.toByteArray(Charsets.UTF_8)
        writeChannel.writeInt(stringBytes.size)
        stringBytes.forEach { writeChannel.writeByte(it) }
    }

    suspend fun readString(): String {
        val stringLengthInBytes = readChannel.readInt()
        val stringBytes = readChannel.readPacket(stringLengthInBytes).readBytes()
        return stringBytes.toString(Charsets.UTF_8)
    }

    suspend fun writeShortString(str: String) {
        val stringBytes = str.toByteArray(Charsets.UTF_8)

        if (stringBytes.size > Byte.MAX_VALUE) {
            throw IllegalArgumentException("[val=$str] Short string max bytes size is ${Byte.MAX_VALUE}")
        }
        writeChannel.writeByte(stringBytes.size)
        stringBytes.forEach { writeChannel.writeByte(it) }
    }

    suspend fun readShortString(): String {
        val stringLengthInBytes = readChannel.readByte()
        val stringBytes = readChannel.readPacket(stringLengthInBytes.toInt()).readBytes()
        return stringBytes.toString(Charsets.UTF_8)
    }

    suspend fun writeStringEncoded(str: String) {
        val stringBytes = str.toByteArray(Charsets.UTF_8)
        val encoded = Base64.getEncoder().encode(stringBytes)
        writeChannel.writeInt(encoded.size)
        encoded.forEach { writeChannel.writeByte(it) }
    }

    suspend fun readStringDecoded(): String {
        val stringLengthInBytes = readChannel.readInt()
        val stringBytes = Base64.getDecoder().decode(readChannel.readPacket(stringLengthInBytes).readBytes())
        return stringBytes.toString(Charsets.UTF_8)
    }

    suspend fun writeHeader(header: Header) {
        writeUUID(header.requestId)
        writeOptionalValue(header.txId, this::writeUUID)
    }

    suspend fun readHeader(): Header {
        val requestId = readUUID()
        val txId = readOptionalValue(this::readUUID)
        return Header(requestId, txId)
    }

    suspend fun writeFilter(filter: FilterDto) {
        writeString(filter.entityType)
        writeOptionalValue(filter.propertyName, this::writeShortString)
        writeOptionalValue(filter.value, this::writePropertyValue)
        writeOptionalValue(filter.skip, writeChannel::writeInt)
        writeOptionalValue(filter.take, writeChannel::writeInt)
    }

    suspend fun readFilter(): FilterDto {
        val entityType = readString()
        val propertyName = readOptionalValue(this::readShortString)
        val value = readOptionalValue(this::readPropertyValue)
        val skip = readOptionalValue(readChannel::readInt)
        val take = readOptionalValue(readChannel::readInt)
        return FilterDto(entityType, propertyName, value, skip, take)
    }

    suspend fun writePage(page: PageDto) {
        writeChannel.writeLong(page.total)
        writeChannel.writeInt(page.skip)
        writeChannel.writeInt(page.take)
        writeChannel.writeInt(page.content.size)
        for (entity in page.content) {
            writeEntity(entity)
        }
    }

    suspend fun readPage(): PageDto {
        val total = readChannel.readLong()
        val skip = readChannel.readInt()
        val take = readChannel.readInt()
        val contentSize = readChannel.readInt()
        val content = mutableListOf<EntityDto>()
        repeat(contentSize) {
            content.add(readEntity())
        }
        return PageDto(total, skip, take, content)
    }

    suspend fun writeTx(tx: TxDto) {
        writeChannel.writeBoolean(tx.readOnly)
    }

    suspend fun readTx(): TxDto {
        val readOnly = readChannel.readBoolean()
        return TxDto(readOnly)
    }

    suspend fun writeBooleanResult(booleanResultDto: BooleanResultDto) {
        writeChannel.writeBoolean(booleanResultDto.result)
    }

    suspend fun readBooleanResult(): BooleanResultDto {
        val result = readChannel.readBoolean()
        return BooleanResultDto(result)
    }

    suspend fun writeEntityId(entityId: String?) {
        val idBytes = entityId?.toByteArray(Charsets.UTF_8)
        val idLength = idBytes?.size ?: 0
        writeChannel.writeByte(idLength)
        idBytes?.let { writeChannel.writeAvailable(ByteBuffer.wrap(idBytes)) }
    }

    suspend fun readEntityId(): String? {
        val idLength = readChannel.readByte()
        return if (idLength > 0) {
            val bytes = readChannel.readPacket(idLength.toInt()).readBytes()
            bytes.toString(Charsets.UTF_8)
        } else {
            null
        }
    }

    suspend fun writeEntity(entity: EntityDto) {
        writeEntityId(entity.id)
        writeString(entity.type)
        writeChannel.writeInt(entity.properties.size)
        for ((name, value) in entity.properties) {
            writeShortString(name)
            writePropertyValue(value)
        }
    }

    suspend fun readEntity(): EntityDto {
        val id = readEntityId()
        val type = readString()
        val propertiesCount = readChannel.readInt()
        val properties = mutableMapOf<String, Comparable<*>>()
        repeat (propertiesCount) {
            val name = readShortString()
            val value = readPropertyValue()
            properties[name] = value
        }
        return EntityDto(id, type, properties)
    }

    private suspend fun writePropertyValue(value: Comparable<*>) {
        when (value) {
            is Boolean -> {
                writeChannel.writeByte(PropertyType.BOOLEAN.code)
                writeChannel.writeBoolean(value)
            }
            is Byte -> {
                writeChannel.writeByte(PropertyType.BYTE.code)
                writeChannel.writeByte(value)
            }
            is Short -> {
                writeChannel.writeByte(PropertyType.SHORT.code)
                writeChannel.writeShort(value)
            }
            is Int -> {
                writeChannel.writeByte(PropertyType.INT.code)
                writeChannel.writeInt(value)
            }
            is Long -> {
                writeChannel.writeByte(PropertyType.LONG.code)
                writeChannel.writeLong(value)
            }
            is Float -> {
                writeChannel.writeByte(PropertyType.FLOAT.code)
                writeChannel.writeFloat(value)
            }
            is Double -> {
                writeChannel.writeByte(PropertyType.DOUBLE.code)
                writeChannel.writeDouble(value)
            }
            is String -> {
                writeChannel.writeByte(PropertyType.STRING.code)
                writeStringEncoded(value)
            }
            else -> throw IllegalArgumentException("Cannot write unsupported property type ${value::class.simpleName}")
        }
    }

    private suspend fun readPropertyValue(): Comparable<*> {
        val typeCode = readChannel.readByte()
        return when (typeCode) {
            PropertyType.BOOLEAN.code -> {
                readChannel.readBoolean()
            }
            PropertyType.BYTE.code -> {
                readChannel.readByte()
            }
            PropertyType.SHORT.code -> {
                readChannel.readShort()
            }
            PropertyType.INT.code -> {
                readChannel.readInt()
            }
            PropertyType.LONG.code -> {
                readChannel.readLong()
            }
            PropertyType.FLOAT.code -> {
                readChannel.readFloat()
            }
            PropertyType.DOUBLE.code -> {
                readChannel.readDouble()
            }
            PropertyType.STRING.code -> {
                readStringDecoded()
            }
            else -> throw IllegalArgumentException("Cannot write unsupported property type code $typeCode")
        }
    }

    private suspend fun <T> writeOptionalValue(value: T?, writeValue: suspend (T) -> Unit) {
        value?.let {
            writeChannel.writeBoolean(true)
            writeValue(value)
        } ?: writeChannel.writeBoolean(false)
    }

    private suspend fun <T> readOptionalValue(readValue: suspend () -> T) : T? {
        val exists = readChannel.readBoolean()
        return if (exists) {
            readValue()
        } else {
            null
        }
    }
}