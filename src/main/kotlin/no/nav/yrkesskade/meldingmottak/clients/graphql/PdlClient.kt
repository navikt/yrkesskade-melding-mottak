package no.nav.yrkesskade.meldingmottak.clients.graphql

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.expediagroup.graphql.generated.HentAdresse
import com.expediagroup.graphql.generated.HentIdenter
import com.expediagroup.graphql.generated.HentPerson
import com.expediagroup.graphql.generated.Long
import com.expediagroup.graphql.generated.enums.IdentGruppe
import com.expediagroup.graphql.generated.hentperson.Person
import kotlinx.coroutines.runBlocking
import no.nav.familie.log.mdc.MDCConstants
import no.nav.yrkesskade.meldingmottak.domene.Adresse
import no.nav.yrkesskade.meldingmottak.domene.Navn
import no.nav.yrkesskade.meldingmottak.util.TokenUtil
import no.nav.yrkesskade.meldingmottak.util.getLogger
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.slf4j.MDC
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
        val identerResult = hentIdenter(fodselsnummer, listOf(IdentGruppe.AKTORID), false)
        return extractAktorId(identerResult)
    }

    /**
     * @param ident fødselsnummer eller aktørId
     */
    fun hentIdenter(ident: String, grupper: List<IdentGruppe>, historikk: Boolean = false): HentIdenter.Result? {
        val token = tokenUtil.getAppAccessTokenWithPdlScope()
        logger.info("Hentet token for Pdl")
        val hentIdenterQuery = HentIdenter(
            HentIdenter.Variables(
                ident = ident,
                grupper = grupper,
                historikk = historikk
            )
        )

        val identerResult: HentIdenter.Result?
        runBlocking {
            logger.info("Henter identer (grupper=$grupper, historikk=$historikk) fra PDL på url $pdlGraphqlUrl")
            secureLogger.info("Henter identer (grupper=$grupper, historikk=$historikk) fra PDL for person med ident $ident på url $pdlGraphqlUrl")
            val response: GraphQLClientResponse<HentIdenter.Result> = client.execute(hentIdenterQuery) {
                headers {
                    it.add(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    it.add("Nav-Call-Id", MDC.get(MDCConstants.MDC_CALL_ID))
                }
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

        return identerResult
    }

    fun hentPerson(foedselsnummer: String): Person? {
        return hentPersonResult(foedselsnummer)?.hentPerson
    }

    fun hentNavn(fodselsnummer: String): Navn? {
        val navnOgAdresse = hentNavnOgAdresse(fodselsnummer, false)
        return navnOgAdresse.first
    }

    fun hentNavnOgAdresse(fodselsnummer: String, hentAdresse: Boolean = true): Pair<Navn?, Adresse?> {
        val personResult = hentPersonResult(fodselsnummer)

        if (personResult == null) {
            logger.info("Fant ikke navn på person")
            secureLogger.info("Fant ikke navn på person med fnr $fodselsnummer")
            return Pair(null, null)
        }

        val navn: Navn? = tilNavn(personResult)
        var adresse: Adresse? = null

        val kanHenteAdresse = kanHenteAdresse()
        if (!kanHenteAdresse) {
            secureLogger.info("Skal ikke hente bostedsadresse for denne personen")
        }

        if (hentAdresse && kanHenteAdresse) {
            val bostedsadresse = personResult.hentPerson?.bostedsadresse?.firstOrNull()

            if (bostedsadresse?.vegadresse?.matrikkelId != null || bostedsadresse?.matrikkeladresse?.matrikkelId != null) {
                adresse = getAdresse(personResult, fodselsnummer)
            }
            else if (bostedsadresse?.ukjentBosted != null) {
                adresse = tilAdresse(bostedsadresse.ukjentBosted)
            }
            else if (bostedsadresse?.utenlandskAdresse != null) {
                adresse = tilAdresse(bostedsadresse.utenlandskAdresse)
            }
        }

        return Pair(navn, adresse)
    }

    private fun getAdresse(personResult: HentPerson.Result, fodselsnummer: String): Adresse? {
        val matrikkelId = extractMatrikkelId(personResult)

        if (matrikkelId != null) {
            val adresseResult = hentAdresse(matrikkelId)

            if (adresseResult == null) {
                logger.info("Fant ikke adresse for matrikkel $matrikkelId")
                secureLogger.info("Fant ikke adresse for matrikkel $matrikkelId for person med fnr $fodselsnummer")
            } else {
                return tilAdresse(adresseResult)
            }
        }

        return null
    }

    private fun kanHenteAdresse(): Boolean {
        // Tidligere skulle ikke bostedsadresse hentes for kode6-personer, men nå kan adresse vises for alle
        return true
    }

    private fun extractAktorId(identerResult: HentIdenter.Result?): String? {
        return identerResult?.hentIdenter?.identer?.stream()
            ?.filter { identInfo -> identInfo.gruppe == IdentGruppe.AKTORID }?.findFirst()?.get()?.ident
    }

    private fun hentPersonResult(fodselsnummer: String): HentPerson.Result? {
        val token = tokenUtil.getAppAccessTokenWithPdlScope()
        logger.info("Hentet token for Pdl")
        val hentPersonQuery = HentPerson(HentPerson.Variables(fodselsnummer))

        val personResult: HentPerson.Result?
        runBlocking {
            logger.info("Henter person fra PDL på url $pdlGraphqlUrl")
            secureLogger.info("Henter person fra PDL for person med fnr $fodselsnummer på url $pdlGraphqlUrl")
            val response: GraphQLClientResponse<HentPerson.Result> = client.execute(hentPersonQuery) {
                headers {
                    it.add(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    it.add("Tema", "YRK")
                    it.add("Nav-Call-Id", MDC.get(MDCConstants.MDC_CALL_ID))
                }
            }
            personResult = response.data
            logger.info("Returnerte fra PDL, se securelogs for detaljer")
            secureLogger.info("Returnerte fra PDL, data: " + response.data)
            if (!response.errors.isNullOrEmpty()) {
                logger.error("Responsen fra PDL inneholder feil! Se securelogs")
                secureLogger.error("Responsen fra PDL inneholder feil: ${response.errors}")
                throw RuntimeException("Responsen fra PDL inneholder feil! Se securelogs")
            }
        }

        return personResult
    }

    private fun hentAdresse(matrikkelId: Long): HentAdresse.Result? {
        val token = tokenUtil.getAppAccessTokenWithPdlScope()
        logger.info("Hentet token for Pdl")
        val hentAdresseQuery = HentAdresse(HentAdresse.Variables(matrikkelId))

        val adresseResult: HentAdresse.Result?
        runBlocking {
            logger.info("Henter adresse fra PDL på url $pdlGraphqlUrl")
            secureLogger.info("Henter adresse fra PDL for matrikkelId $matrikkelId på url $pdlGraphqlUrl")
            val response: GraphQLClientResponse<HentAdresse.Result> = client.execute(hentAdresseQuery) {
                headers {
                    it.add(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    it.add("Nav-Call-Id", MDC.get(MDCConstants.MDC_CALL_ID))
                }
            }
            adresseResult = response.data
            logger.info("Returnerte fra PDL, se securelogs for detaljer")
            secureLogger.info("Returnerte fra PDL, data: " + response.data)
            if (!response.errors.isNullOrEmpty()) {
                logger.error("Responsen fra PDL inneholder feil! Se securelogs")
                secureLogger.error("Responsen fra PDL inneholder feil: ${response.errors}")
                throw RuntimeException("Responsen fra PDL inneholder feil! Se securelogs")
            }
        }

        return adresseResult
    }

}


