package no.nav.yrkesskade.meldingmottak.services

import com.expediagroup.graphql.generated.enums.AdressebeskyttelseGradering
import com.expediagroup.graphql.generated.enums.IdentGruppe
import com.expediagroup.graphql.generated.enums.Sakstype
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.hentperson.Adressebeskyttelse
import com.expediagroup.graphql.generated.hentperson.Person
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.SafClient
import no.nav.yrkesskade.meldingmottak.clients.infotrygd.InfotrygdClient
import no.nav.yrkesskade.meldingmottak.clients.tilgang.SkjermedePersonerClient
import org.springframework.stereotype.Service

@Service
class RutingService(
    private val pdlClient: PdlClient,
    private val safClient: SafClient,
    private val skjermedePersonerClient: SkjermedePersonerClient,
    private val infotrygdClient: InfotrygdClient
) {

    fun utfoerRuting(foedselsnummer: String): Rute {
        check(foedselsnummer.isNotBlank()) { "Det må angis et fødselsnummer for å utføre ruting!" }

        //    TODO: Pass på at resultater caches.

        val person = pdlClient.hentPerson(foedselsnummer)

        if (person == null
            || erDoed(person)
            || erKode7Fortrolig(person)
            || erKode6StrengtFortrolig(person)
            || erEgenAnsatt(foedselsnummer)
            || harAapenGenerellYrkesskadeSak(foedselsnummer)
        ) {
            return Rute.GOSYS_OG_INFOTRYGD
        }

        val foedselsnumreMedHistorikk = hentFoedselsnumreMedHistorikk(foedselsnummer)
        if (harEksisterendeInfotrygdSak(foedselsnumreMedHistorikk) || harPotensiellKommendeSak(foedselsnummer)) {
            return Rute.GOSYS_OG_INFOTRYGD
        }

        return Rute.GOSYS_OG_INFOTRYGD  // TODO: Test først at ulike forretningsregler gir riktig resultat i logger, åpne deretter for at ruting kan gå til nytt saksbehandlingssystem
    }



    /* Hjelpefunksjoner */

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

    internal fun harAapenGenerellYrkesskadeSak(foedselsnummer: String): Boolean {
        val sakerForPerson = safClient.hentSakerForPerson(foedselsnummer)?.saker ?: emptyList()
        val generelleYrkesskadesaker =
            sakerForPerson.filter { sak -> sak?.tema == Tema.YRK && sak.sakstype == Sakstype.GENERELL_SAK }
        return generelleYrkesskadesaker.isNotEmpty()
    }

    internal fun harEksisterendeInfotrygdSak(foedselsnumre: List<String>): Boolean {
        return infotrygdClient.harEksisterendeSak(foedselsnumre)
    }

    internal fun harPotensiellKommendeSak(foedselsnummer: String): Boolean {
        TODO("Not yet implemented")
    }

    internal fun hentFoedselsnumreMedHistorikk(foedselsnummer: String): List<String> {
        val identerMedHistorikk = pdlClient.hentIdenter(foedselsnummer, listOf(IdentGruppe.FOLKEREGISTERIDENT), true)

        return identerMedHistorikk?.hentIdenter?.identer?.map { it.ident }?.toList() ?: listOf(foedselsnummer)
    }


    enum class Rute {
        GOSYS_OG_INFOTRYGD,
        YRKESSKADE_SAKSBEHANDLING
    }

}