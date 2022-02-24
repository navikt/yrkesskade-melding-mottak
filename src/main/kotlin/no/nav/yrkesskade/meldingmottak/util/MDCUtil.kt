package no.nav.yrkesskade.meldingmottak.util

import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.MDC
import java.util.UUID


/**
 * MVP-aktig setting av callId. Setter callId før funksjonskall, fjerner den etterpå.
 * Skal kun brukes helt på starten av en hendelse.
 * Bruker MDCConstants.MDC_CALL_ID foreløpig, fordi [no.nav.yrkesskade.prosessering.domene.Task] bruker det.
 *
 * @param callId callIden som evt. kommer fra et annet system; ellers random UUID
 * @param funksjon funksjonen som skal kalles på
 */
fun kallMetodeMedCallId(
    callId: String? = UUID.randomUUID().toString(),
    funksjon: () -> Unit
) {
    MDC.put(MDCConstants.MDC_CALL_ID, callId)
    funksjon.invoke()
    MDC.remove(MDCConstants.MDC_CALL_ID)
}