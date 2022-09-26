package no.nav.yrkesskade.meldingmottak.clients

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.util.getLogger
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.springframework.web.reactive.function.client.WebClientResponseException

abstract class AbstractRestClient(val klientnavn: String) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        internal val log = getLogger(javaClass.enclosingClass)
        internal val secureLogger = getSecureLogger()
        internal val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    }

    @Suppress("SameParameterValue")
    fun <T> logTimingAndWebClientResponseException(methodName: String, function: () -> T): T {
        val start: Long = System.currentTimeMillis()
        try {
            return function.invoke()
        } catch (ex: WebClientResponseException) {
            secureLogger.error(
                "Got a {} error calling $klientnavn {} {} with message {}",
                ex.statusCode,
                ex.request?.method ?: "-",
                ex.request?.uri ?: "-",
                ex.responseBodyAsString
            )
            throw ex
        } catch (rtex: RuntimeException) {
            log.warn("Caught RuntimeException while calling $klientnavn", rtex)
            throw rtex
        } finally {
            val end: Long = System.currentTimeMillis()
            log.info("Method {} took {} millis", methodName, (end - start))
        }
    }
}