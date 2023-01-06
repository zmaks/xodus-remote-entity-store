package com.zheltoukhov.xres.protocol

import com.zheltoukhov.xres.protocol.command.*


enum class CommandType(
    val code: Byte,
    val command: Command<out RequestPayload, out ResponsePayload>
) {
    BEGIN_TX(1, BeginTxCommand),
    COMMIT(2, CommitCommand),
    FLUSH(3, FlushCommand),
    ABORT(4, AbortCommand),

    CREATE(11, CreateCommand),
    UPDATE(12, UpdateCommand),
    DELETE(13, DeleteCommand),
    GET(14, GetCommand),
    FIND(21, FindCommand);

    companion object {
        private val CODE_MAP = CommandType.values().associateBy { it.code }
        fun fromCode(code: Byte): CommandType? = CODE_MAP[code]
    }
}