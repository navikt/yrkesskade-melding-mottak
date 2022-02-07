package no.nav.yrkesskade.meldingmottak.clients

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.Journalpost
import kotlinx.coroutines.runBlocking
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import no.nav.yrkesskade.meldingmottak.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.ws.rs.core.HttpHeaders

/**
 * Klient for å hente oppdatert journalpost fra saf (Sak og arkiv fasade)
 */
@Component
class SafClient(@Value("\${saf.graphql.url}") private val safGraphqlUrl: String,
                private val tokenUtil: TokenUtil
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
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
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }
            oppdatertJournalpost = response.data
            if (!response.errors.isNullOrEmpty()) {
                logger.error("SAF response errors: ${response.errors}")
                throw RuntimeException(response.errors.toString())
            }
        }
        return oppdatertJournalpost
    }
}