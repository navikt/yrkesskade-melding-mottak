package no.nav.yrkesskade.meldingmottak.clients.graphql

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.Journalpost
import com.expediagroup.graphql.generated.Journalposter
import com.expediagroup.graphql.generated.Saker
import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.enums.Journalstatus
import kotlinx.coroutines.runBlocking
import no.nav.familie.log.mdc.MDCConstants
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import no.nav.yrkesskade.meldingmottak.util.getLogger
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.ws.rs.core.HttpHeaders


/**
 * Klient for å hente oppdatert journalpost fra saf (Sak og arkiv fasade)
 */
@Suppress("UastIncorrectHttpHeaderInspection")
@Component
class SafClient(
    @Value("\${saf.graphql.url}") private val safGraphqlUrl: String,
    @Value("\${spring.application.name}") val applicationName: String,
    private val tokenUtil: TokenUtil
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    private val client = GraphQLWebClient(url = safGraphqlUrl)

    fun hentOppdatertJournalpost(journalpostId: String): Journalpost.Result? {
        val token = tokenUtil.getAppAccessTokenWithSafScope()
        logger.info("Hentet token for Saf")
        val journalpostQuery = Journalpost(Journalpost.Variables(journalpostId))

        logger.info("Henter oppdatert journalpost for id $journalpostId på url $safGraphqlUrl")
        val oppdatertJournalpost: Journalpost.Result?
        runBlocking {
            val response: GraphQLClientResponse<Journalpost.Result> = client.execute(journalpostQuery) {
                headers {
                    it.add(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    it.add("Nav-Callid", MDC.get(MDCConstants.MDC_CALL_ID))
                    it.add("Nav-Consumer-Id", applicationName)
                }
            }
            oppdatertJournalpost = response.data
            if (!response.errors.isNullOrEmpty()) {
                secureLogger.error("Responsen fra SAF inneholder feil: ${response.errors}")
                throw RuntimeException("Responsen fra SAF inneholder feil! Se securelogs")
            }
        }
        return oppdatertJournalpost
    }

    fun hentJournalposterForPerson(foedselsnummer: String): Journalposter.Result? {
        val token = tokenUtil.getAppAccessTokenWithSafScope()
        logger.info("Hentet token for Saf")
        val journalposterQuery = Journalposter(
            Journalposter.Variables(
                foedselsnummer,
                BrukerIdType.FNR,
                listOf(Journalstatus.MOTTATT, Journalstatus.UNDER_ARBEID, Journalstatus.JOURNALFOERT)
            )
        )

        logger.info("Henter journalposter for person på url $safGraphqlUrl")
        secureLogger.info("Henter journalposter for personen $foedselsnummer på url $safGraphqlUrl")
        val result: Journalposter.Result?
        runBlocking {
            val response: GraphQLClientResponse<Journalposter.Result> = client.execute(journalposterQuery) {
                headers {
                    it.add(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    it.add("Nav-Callid", MDC.get(MDCConstants.MDC_CALL_ID))
                    it.add("Nav-Consumer-Id", applicationName)
                }
            }
            result = response.data
            if (!response.errors.isNullOrEmpty()) {
                secureLogger.error("Responsen fra SAF inneholder feil: ${response.errors}")
                throw RuntimeException("Responsen fra SAF inneholder feil! Se securelogs")
            }
        }
        return result
    }

    fun hentSakerForPerson(foedselsnummer: String): Saker.Result? {
        val token = tokenUtil.getAppAccessTokenWithSafScope()
        logger.info("Hentet token for Saf")
        val sakerQuery = Saker(Saker.Variables(foedselsnummer))

        logger.info("Henter saker for person NN på url $safGraphqlUrl")
        secureLogger.info("Henter saker for person $foedselsnummer på url $safGraphqlUrl")
        val saker: Saker.Result?
        runBlocking {
            val response: GraphQLClientResponse<Saker.Result> = client.execute(sakerQuery) {
                headers {
                    it.add(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    it.add("Nav-Callid", MDC.get(MDCConstants.MDC_CALL_ID))
                    it.add("Nav-Consumer-Id", applicationName)
                }
            }
            saker = response.data
            if (!response.errors.isNullOrEmpty()) {
                secureLogger.error("Responsen fra SAF inneholder feil: ${response.errors}")
                throw RuntimeException("Responsen fra SAF inneholder feil! Se securelogs")
            }
        }
        return saker
    }
}