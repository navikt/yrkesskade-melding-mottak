package no.nav.yrkesskade.meldingmottak.clients.arbeidsfordeling

import no.nav.familie.log.mdc.MDCConstants
import no.nav.yrkesskade.meldingmottak.clients.AbstractRestClient
import no.nav.yrkesskade.meldingmottak.util.getLogger
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

/**
 * # NORG2 tjeneste
 *
 * ## Dokumentasjon
 * Det finnes p.t. ingen oppdatert dokumentasjon for denne rest-tjenesten. Nærmeste dokumentasjon er
 *  - https://confluence.adeo.no/display/FEL/NORG2+-+Webservice+ArbeidsfordelingV1
 *  - https://github.com/navikt/team-org-doc/blob/main/src/main/asciidoc/applikasjoner.adoc
 *
 * ## Swagger
 *  - https://norg2.dev.adeo.no/norg2/swagger-ui.html?urls.primaryName=API%20versjon%201#/arbeidsfordeling/getBehandlendeEnheterUsingPOST
 *
 */
@Component
class ArbeidsfordelingClient(
    private val arbeidsfordelingWebClient: WebClient,
    @Value("\${spring.application.name}") val applicationName: String
) : AbstractRestClient("Arbeidsfordeling") {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Retryable
    fun finnBehandlendeEnhetMedBesteMatch(kriterie: ArbeidsfordelingKriterie): ArbeidsfordelingResponse? {
        log.info("Finn behandlende enhet for person")
        secureLogger.info("Finn behandlende enhet for person med kriterier tema=${kriterie.tema} geografiskOmraade=${kriterie.geografiskOmraade} diskresjonskode=${kriterie.diskresjonskode} skjermet=${kriterie.skjermet}")
        return logTimingAndWebClientResponseException("finn behandlende enhet") {
            arbeidsfordelingWebClient.post()
                .uri("api/v1/arbeidsfordeling/enheter/bestmatch")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Nav-Callid", MDC.get(MDCConstants.MDC_CALL_ID))
                .header("Nav-Consumer-Id", applicationName)
                .bodyValue(kriterie)
                .retrieve()
                .bodyToMono<ArbeidsfordelingResponse>()
                .block() ?: throw RuntimeException("Kunne ikke finne behandlende enhet")
        }.also { response ->
            if (!response?.enheter.isNullOrEmpty()) {
                log.info("Fant behandlende enhet")
                secureLogger.info("Fant behandlende enhet ${response.enheter.first().enhetNr} ${response.enheter.first().navn}")
            } else {
                log.info("Fant ingen behandlende enhet")
                secureLogger.info("Fant ingen behandlende enhet")
            }
        }
    }

}