package no.nav.yrkesskade.meldingmottak.clients.graphql

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.HentIdenter
import com.expediagroup.graphql.generated.enums.IdentGruppe
import kotlinx.coroutines.runBlocking
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import no.nav.yrkesskade.meldingmottak.util.getLogger
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.ws.rs.core.HttpHeaders

/**
 * Klient for å hente ut personinfo fra PDL (Persondataløsningen)
 */
@Component
class PdlClient(
    @Value("\${pdl.graphql.url}") private val pdlGraphqlUrl: String,
    private val tokenUtil: TokenUtil
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    private val client = GraphQLWebClient(url = pdlGraphqlUrl)

    fun hentAktorId(fodselsnummer: String): String? {
        val token = tokenUtil.getAppAccessTokenWithPdlScope()
        logger.info("Hentet token for Pdl")
        val hentIdenterQuery = HentIdenter(HentIdenter.Variables(fodselsnummer))

        val identerResult: HentIdenter.Result?
        runBlocking {
            logger.info("Henter aktørId fra PDL på url $pdlGraphqlUrl")
            secureLogger.info("Henter aktørId fra PDL for person med fnr $fodselsnummer på url $pdlGraphqlUrl")
            val response: GraphQLClientResponse<HentIdenter.Result> = client.execute(hentIdenterQuery) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }
            identerResult = response.data
            logger.info("Returnerte fra PDL, se securelogs for detaljer")
            secureLogger.info("Returnerte fra PDL, data: " + response.data)
            if (!response.errors.isNullOrEmpty()) {
                logger.error("Responsen fra PDL inneholder feil! Se securelogs")
                secureLogger.error("Responsen fra PDL inneholder feil: ${response.errors}")
                throw RuntimeException("Responsen fra PDL inneholder feil! Se securelogs")
            }
        }

        return extractAktorId(identerResult)
    }

    private fun extractAktorId(identerResult: HentIdenter.Result?): String? {
        return identerResult?.hentIdenter?.identer?.stream()
            ?.filter { identInfo -> identInfo.gruppe == IdentGruppe.AKTORID }?.findFirst()?.get()?.ident
    }

}