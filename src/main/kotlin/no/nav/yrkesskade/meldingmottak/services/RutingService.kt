package no.nav.yrkesskade.meldingmottak.services

import com.expediagroup.graphql.generated.enums.AdressebeskyttelseGradering
import com.expediagroup.graphql.generated.enums.IdentGruppe
import com.expediagroup.graphql.generated.hentperson.Adressebeskyttelse
import com.expediagroup.graphql.generated.hentperson.Person
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.tilgang.SkjermedePersonerClient
import org.springframework.stereotype.Service

@Service
class RutingService(
    private val pdlClient: PdlClient,
    private val skjermedePersonerClient: SkjermedePersonerClient
) {

    fun utfoerRuting(foedselsnummer: String): Rute {

        //    TODO: Pass på at resultater caches.

        val person = pdlClient.hentPerson(foedselsnummer)

        if (person == null || erDoed(person) || erKode7Fortrolig(person) || erKode6StrengtFortrolig(person) || erEgenAnsatt(foedselsnummer)) {
            return Rute.GOSYS_OG_INFOTRYGD
        }

        val identerMedHistorikk = pdlClient.hentIdenter(foedselsnummer, listOf(IdentGruppe.FOLKEREGISTERIDENT), true)
        if (harEksisterendeSak(foedselsnummer) || harPotensiellKommendeSak(foedselsnummer)) {
            return Rute.GOSYS_OG_INFOTRYGD
        }

        // Hopp videre ved første mulighet for å slippe unødvendige oppslag


        return Rute.GOSYS_OG_INFOTRYGD
    }

    internal fun erDoed(person: Person): Boolean =
        person.doedsfall.isNotEmpty()


    internal fun erKode7Fortrolig(person: Person): Boolean {
        return person.adressebeskyttelse.contains(Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG))
    }

    internal fun erKode6StrengtFortrolig(person: Person): Boolean {
        return person.adressebeskyttelse.contains(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG)) ||
                person.adressebeskyttelse.contains(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND))
    }

    internal fun erEgenAnsatt(foedselsnummer: String): Boolean {
        val request = SkjermedePersonerClient.SkjermedePersonerRequest(listOf(foedselsnummer))
        val response = skjermedePersonerClient.erSkjermet(request)
        return response.skjermedePersonerMap[foedselsnummer] ?: false
    }

    internal fun harEksisterendeSak(foedselsnummer: String): Boolean {
        return false
    }

    internal fun harPotensiellKommendeSak(foedselsnummer: String): Boolean {
        TODO("Not yet implemented")
    }


    enum class Rute {
        GOSYS_OG_INFOTRYGD,
        YRKESSKADE_SAKSBEHANDLING
    }

}