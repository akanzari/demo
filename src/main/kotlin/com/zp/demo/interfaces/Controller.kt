package com.zp.demo.interfaces

import com.zp.demo.domains.api.ExternalId
import com.zp.demo.domains.api.RequestConfirmPayer
import com.zp.demo.utils.Status
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.ZonedDateTime
import java.util.*
import javax.validation.Valid
import javax.xml.datatype.DatatypeConfigurationException
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

fun newXMLGregorianCalendar(): XMLGregorianCalendar {
    val df: DatatypeFactory
    try {
        df = DatatypeFactory.newInstance()
    } catch (e: DatatypeConfigurationException) {
        throw IllegalStateException("Error while trying to obtain a new instance of DatatypeFactory", e)
    }
    return df.newXMLGregorianCalendar(GregorianCalendar.from(ZonedDateTime.now()))
}

@RestController
@RequestMapping("/hps")
class HpsController(
    private val commandGateway: ReactorCommandGateway
) {

    @PostMapping
    fun confirmPayerClient(@RequestBody @Valid action: ConfirmPayerClientRequest): Mono<ExternalId> {
        return commandGateway.send<Status<ExternalId>>(
            RequestConfirmPayer(
                externalId = ExternalId(),
                rqUID = action.rqUID,
                phoneNumber = action.phoneNumber,
                dueDate = newXMLGregorianCalendar()
            )
        ).handleStatus()
    }

}