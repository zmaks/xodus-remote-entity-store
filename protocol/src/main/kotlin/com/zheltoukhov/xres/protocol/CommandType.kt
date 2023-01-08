package com.zheltoukhov.xres.protocol


enum class CommandType(
    val code: Byte
) {
    BEGIN_TX(1),
    COMMIT(2),
    FLUSH(3),
    ABORT(4),

    CREATE(11),
    UPDATE(12),
    DELETE(13),
    GET(14),
    FIND(21);

    companion object {
        private val CODE_MAP = CommandType.values().associateBy { it.code }
        fun fromCode(code: Byte): CommandType? = CODE_MAP[code]
    }
}