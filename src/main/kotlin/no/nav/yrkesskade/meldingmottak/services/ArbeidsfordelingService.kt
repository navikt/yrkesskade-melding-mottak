package no.nav.yrkesskade.meldingmottak.services

import com.expediagroup.graphql.generated.enums.AdressebeskyttelseGradering
import com.expediagroup.graphql.generated.hentperson.Adressebeskyttelse
import com.expediagroup.graphql.generated.hentperson.Person
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.yrkesskade.meldingmottak.clients.arbeidsfordeling.ArbeidsfordelingClient
import no.nav.yrkesskade.meldingmottak.clients.arbeidsfordeling.ArbeidsfordelingKriterie
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.tilgang.SkjermedePersonerClient
import no.nav.yrkesskade.meldingmottak.clients.tilgang.SkjermetPersonRequest
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ArbeidsfordelingService(
    private val arbeidsfordelingClient: ArbeidsfordelingClient,
    private val pdlClient: PdlClient,
    private val skjermedePersonerClient: SkjermedePersonerClient
) {

    companion object {
        const val KODE_6_STRENGT_FORTROLIG = "SPSF"
        const val KODE_7_FORTROLIG = "SPFO"

        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun finnBehandlendeEnhetForPerson(foedselsnummer: String): Enhet {

        val person = pdlClient.hentPerson(foedselsnummer)
        if (person == null) {
            secureLogger.error("Fant ikke personen med fødselsnummer $foedselsnummer i pdl.")
            throw RuntimeException("Fant ikke personen i pdl. Se secure log.")
        }

        val arbeidsfordelingskriterie = ArbeidsfordelingKriterie(
            tema = "YRK",
            geografiskOmraade = geografiskOmraade(person),
            diskresjonskode = diskresjonskode(person),
            skjermet = skjermet(foedselsnummer)
        )

        val response = arbeidsfordelingClient.finnBehandlendeEnhetMedBesteMatch(arbeidsfordelingskriterie)
        val enhet = response.enheter.firstOrNull()

        return Enhet(
            enhetId = enhet?.enhetNr ?: "9999",
            enhetNavn = enhet?.navn ?: ""
        )
    }


    internal fun geografiskOmraade(person: Person): String? {
        return person.bostedsadresse.firstOrNull()?.vegadresse?.kommunenummer
    }

    internal fun diskresjonskode(person: Person): String? {
        if (erKode7Fortrolig(person)) {
            return KODE_7_FORTROLIG
        }
        else if (erKode6StrengtFortrolig(person)) {
            return KODE_6_STRENGT_FORTROLIG
        }
        return null
    }

    internal fun skjermet(foedselsnummer: String): Boolean {
        val request = SkjermetPersonRequest(foedselsnummer)
        return skjermedePersonerClient.erSkjermet(request)
    }

    private fun erKode7Fortrolig(person: Person): Boolean {
        return person.adressebeskyttelse.contains(Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG))
    }

    private fun erKode6StrengtFortrolig(person: Person): Boolean {
        return (person.adressebeskyttelse.contains(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG)) ||
                person.adressebeskyttelse.contains(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND)))
    }

}