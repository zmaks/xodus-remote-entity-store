package com.zheltoukhov.xres.protocol.command

import com.zheltoukhov.xres.protocol.EmptyPayload
import com.zheltoukhov.xres.protocol.Protocol
import com.zheltoukhov.xres.protocol.dto.BooleanResultDto
import com.zheltoukhov.xres.protocol.dto.TxDto

object AbortCommand : Command<EmptyPayload, EmptyPayload>()