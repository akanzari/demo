package com.zp.demo.domains.api

import java.util.*

data class ExternalId(val value: UUID = UUID.randomUUID()) {
    companion object {
        fun of(value: String) = ExternalId(UUID.fromString(value))
    }

    fun asReference() = value.toString()
}