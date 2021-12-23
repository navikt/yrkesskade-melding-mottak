package no.nav.yrkesskade.meldingmottak.clients

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.HentIdenter
import com.expediagroup.graphql.generated.enums.IdentGruppe
import kotlinx.coroutines.runBlocking
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.lang.invoke.MethodHandles
import javax.ws.rs.core.HttpHeaders

/**
 * Klient for å hente ut personinfo fra PDL (Persondataløsningen)
 */
@Component
class PdlClient(
    @Value("\${pdl.graphql.url}") private val pdlGraphqlUrl: String,
    private val tokenUtil: TokenUtil
) {

    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val client = GraphQLWebClient(url = pdlGraphqlUrl)

    fun hentAktorId(fodselsnummer: String): String? {
        log.info("pdl: Kaller hentAktorId med fnr $fodselsnummer")

        val token = tokenUtil.getAppAccessTokenWithPdlScope()
        log.info("pdl: Hentet token for Pdl $token")
        val hentIdenterQuery = HentIdenter(HentIdenter.Variables(fodselsnummer))

        val identerResult: HentIdenter.Result?
        runBlocking {
            log.info("Pdl: Henter aktørId for person med fnr $fodselsnummer på url $pdlGraphqlUrl")
            val response: GraphQLClientResponse<HentIdenter.Result> = client.execute(hentIdenterQuery) {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }
            identerResult = response.data
            log.info("Pdl: Ferdig med kallet")
            log.info(response.toString())
            log.info("Pdl: Data: " + response.data)
            log.info("Pdl: Errors: " + response.errors)
            if (!response.errors.isNullOrEmpty()) {
                log.error("PDL response errors: ${response.errors}")
                // TODO: 23/12/2021 Feilhåndtering
            }
        }

        val aktorId = extractAktorId(identerResult)
        log.info("Pdl: Hentet aktørId $aktorId for fnr $fodselsnummer")
        return aktorId
    }

    private fun extractAktorId(identerResult: HentIdenter.Result?): String? {
        return identerResult?.hentIdenter?.identer?.stream()
            ?.filter { identInfo -> identInfo.gruppe == IdentGruppe.AKTORID }?.findFirst()?.get()?.ident
    }

}