package com.zp.demo.domains.api

import com.zp.demo.utils.Command
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.time.Instant
import javax.xml.datatype.XMLGregorianCalendar

sealed interface HpsCommand : Command {
    val externalId: ExternalId
}

data class RequestConfirmPayer(
    @TargetAggregateIdentifier override val externalId: ExternalId,
    val rqUID: String,
    val phoneNumber: String,
    val dueDate: XMLGregorianCalendar?,
    override val timestamp: Instant = Instant.now()
) : HpsCommand