package com.zheltoukhov.xres.protocol.dto

import com.zheltoukhov.xres.protocol.RequestPayload

data class TxDto(
    val readOnly: Boolean
) : RequestPayload