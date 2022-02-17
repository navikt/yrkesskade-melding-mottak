package no.nav.yrkesskade.meldingmottak.clients.dokarkiv

import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.journalpost.Bruker
import no.nav.familie.log.mdc.MDCConstants
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class DokarkivClient(
    private val dokarkivWebClient: WebClient,
    private val tokenUtil: TokenUtil,
    @Value("\${spring.application.name}") val applicationName: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Retryable
    fun journalfoerSkademelding(opprettJournalpostRequest: OpprettJournalpostRequest) {
        log.info("Journalfører skademelding")
        return logTimingAndWebClientResponseException("journalførSkademelding") {
            dokarkivWebClient.post()
                .uri { uriBuilder ->
                    uriBuilder.pathSegment("rest")
                        .pathSegment("journalpostapi")
                        .pathSegment("v1")
                        .pathSegment("journalpost")
                        .build()
                }
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer ${tokenUtil.getAppAccessTokenWithDokarkivScope()}")
                .header("X-Correlation-ID", MDCConstants.MDC_CALL_ID)
                .header("Nav-Consumer-Id", applicationName)
                .bodyValue(opprettJournalpostRequest)
                .retrieve()
                .bodyToMono<OpprettJournalpostResponse>()
                .block() ?: throw RuntimeException("Kunne ikke journalføre skademelding")
        }
    }

    private fun <T> logTimingAndWebClientResponseException(methodName: String, function: () -> T): T {
        val start: Long = System.currentTimeMillis()
        try {
            val result = function.invoke()
            log.info("Opprettet journalpostoppgave")
            return result
        } catch (ex: WebClientResponseException) {
            secureLogger.error(
                "Got a {} error calling Dokarkiv {} {} with message {}",
                ex.statusCode,
                ex.request?.method ?: "-",
                ex.request?.uri ?: "-",
                ex.responseBodyAsString
            )
            throw ex
        } catch (rtex: RuntimeException) {
            log.warn("Caught RuntimeException", rtex)
            throw rtex
        } finally {
            val end: Long = System.currentTimeMillis()
            log.info("Method {} took {} millis", methodName, (end - start))
        }
    }
}

data class OpprettJournalpostResponse(
    val journalpostferdigstilt: Boolean,
    val journalpostId: String,
    val dokumenter: List<DokumentInfoId>
)

data class DokumentInfoId(
    val dokumentInfoId: String
)

data class OpprettJournalpostRequest(
//    val forsoekFerdigstill: Boolean,
    val journalposttype: Journalposttype,
    val avsenderMottaker: AvsenderMottaker,
    val bruker: Bruker,
    val tema: String? = "YRK",
    val kanal: String? = "NAV_NO",
//    val eksternReferanseId: String -- her kan vi evt legge vår egen skademeldingsId.
//val datoMottatt: LocalDate -- sette til dato fra kafka
    val dokumenter: List<Dokument>
)

data class AvsenderMottaker(
    val id: String,
    val idType: BrukerIdType,
)

enum class Journalposttype {
    INNGAAENDE, UTGAAENDE, NOTAT
}

enum class Filtype {
    PDFA,
    JSON
}

enum class Dokumentvariantformat {
    ORIGINAL,
    ARKIV
}

data class Dokument(
    val brevkode: String?,
    val tittel: String?,
    val dokumentvarianter: List<Dokumentvariant>
)

data class Dokumentvariant(
    val filtype: Filtype,
    val variantformat: Dokumentvariantformat,
    val fysiskDokument: ByteArray
)