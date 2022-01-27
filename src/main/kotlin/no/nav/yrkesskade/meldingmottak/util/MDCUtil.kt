package no.nav.yrkesskade.meldingmottak.util

import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.MDC
import java.util.UUID


/**
 * MVP-aktig setting av correlation. Setter correlationId før funksjonskall, fjerner den etterpå.
 * Skal kun brukes helt på starten av en hendelse.
 * Bruker MDCConstants.MDC_CALL_ID foreløpig, fordi [no.nav.yrkesskade.prosessering.domene.Task] bruker det.
 *
 * @param funksjon funksjonen som skal kalles på
 */
fun kallMetodeMedCorrelation(funksjon: () -> Unit) {
    MDC.put(MDCConstants.MDC_CALL_ID, UUID.randomUUID().toString())
    funksjon.invoke()
    MDC.remove(MDCConstants.MDC_CALL_ID)
}