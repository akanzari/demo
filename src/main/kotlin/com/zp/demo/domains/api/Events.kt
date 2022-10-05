package com.zp.demo.domains.api

import com.zp.demo.utils.Event
import java.time.Instant
import javax.xml.datatype.XMLGregorianCalendar

sealed interface HpsEvent : Event {
    val externalId: ExternalId
}

data class ConfirmPayerRequested(
    override val externalId: ExternalId,
    val rqUID: String,
    val phoneNumber: String,
    val dueDate: XMLGregorianCalendar?,
    override val timestamp: Instant = Instant.now()
) : HpsEvent