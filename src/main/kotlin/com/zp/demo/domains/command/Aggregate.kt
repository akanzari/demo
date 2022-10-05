package com.zp.demo.domains.command

import com.zp.demo.domains.api.ConfirmPayerRequested
import com.zp.demo.domains.api.ExternalId
import com.zp.demo.domains.api.RequestConfirmPayer
import com.zp.demo.utils.Status
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
internal class PayerAggregate() {

    @AggregateIdentifier
    private lateinit var externalId: ExternalId

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    fun handle(command: RequestConfirmPayer): Status<ExternalId> = Status.of {
        AggregateLifecycle.apply(
            ConfirmPayerRequested(
                externalId = command.externalId,
                rqUID = command.rqUID,
                phoneNumber = command.phoneNumber,
                dueDate = command.dueDate,
                timestamp = command.timestamp
            )
        )
        command.externalId
    }

    @EventSourcingHandler
    fun on(event: ConfirmPayerRequested) {
        externalId = event.externalId
    }

}