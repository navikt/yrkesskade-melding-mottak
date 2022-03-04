package no.nav.yrkesskade.meldingmottak.clients

import no.nav.familie.log.mdc.MDCConstants
import no.nav.yrkesskade.meldingmottak.pdf.domene.PdfSkademelding
import no.nav.yrkesskade.meldingmottak.services.PdfTemplate
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class PdfClient(
    private val pdfWebClient: WebClient
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Retryable
    fun lagPdf(pdfSkademelding: PdfSkademelding, template: PdfTemplate): ByteArray {
        log.info("Lager pdf av typen ${template.templatenavn}")
        return logTimingAndWebClientResponseException("lagPdf") {
            pdfWebClient.post()
                .uri { uriBuilder ->
                    uriBuilder.pathSegment("template")
                        .pathSegment("${template.templatenavn}")
                        .pathSegment("download-pdf")
                        .build()
                }
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pdfSkademelding)
                .retrieve()
                .bodyToMono<ByteArray>()
                .block() ?: throw RuntimeException("Kunne ikke lage pdf")
        }.also {
            log.info("Opprettet pdf $template")
        }
    }

    private fun <T> logTimingAndWebClientResponseException(methodName: String, function: () -> T): T {
        val start: Long = System.currentTimeMillis()
        try {
            return function.invoke()
        } catch (ex: WebClientResponseException) {
            secureLogger.error(
                "Got a {} error calling Pdf Dokgen {} {} with message {}",
                ex.statusCode,
                ex.request?.method ?: "-",
                ex.request?.uri ?: "-",
                ex.responseBodyAsString
            )
            throw ex
        } catch (rtex: RuntimeException) {
            log.warn("Caught RuntimeException while calling Pdf Dokgen", rtex)
            throw rtex
        } finally {
            val end: Long = System.currentTimeMillis()
            log.info("Method {} took {} millis", methodName, (end - start))
        }
    }
}