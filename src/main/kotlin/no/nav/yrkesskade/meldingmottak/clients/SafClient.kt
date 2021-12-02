package no.nav.yrkesskade.meldingmottak.clients

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.Journalpost
import kotlinx.coroutines.runBlocking
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.lang.invoke.MethodHandles
import javax.ws.rs.core.HttpHeaders

@Component
class SafClient(@Value("\${saf.graphql.url}") private val safGraphqlUrl: String,
                private val tokenUtil: TokenUtil
) {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val client = GraphQLWebClient(url = safGraphqlUrl)

    fun hentOppdatertJournalpost(journalpostId: String): Journalpost.Result? {
        val token = tokenUtil.getAppAccessTokenWithSafScope()
        log.info("Hentet token")
        val journalpostQuery = Journalpost(Journalpost.Variables(journalpostId))

        log.info("Henter oppdatert journalpost for id $journalpostId på url $safGraphqlUrl")
        val oppdatertJournalpost: Journalpost.Result?
        runBlocking {
            val response: GraphQLClientResponse<Journalpost.Result> = client.execute(journalpostQuery) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }
            oppdatertJournalpost = response.data
            log.info("SAF Response errors: ${response.errors}")
        }
        return oppdatertJournalpost
    }
}