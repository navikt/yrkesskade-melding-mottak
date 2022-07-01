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
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RutingService(
    private val pdlClient: PdlClient,
    private val safClient: SafClient,
    private val skjermedePersonerClient: SkjermedePersonerClient,
    private val infotrygdClient: InfotrygdClient
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    /**
     * Kontrollerer om en hendelse (f.eks. en innkommende tannlegeerklæring) for en angitt person, skal rutes til
     * gammelt eller nytt saksbehandlingssystem.
     *
     * Gammelt saksbehandlingssystem = Gosys/Infotrygd
     * Nytt saksbehandlingssystem = Kompys
     *
     * OBS! Rekkefølgen på noen av kontrollene har betydning, så ikke endre på rekkefølgen.
     */
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

        // TODO: Test først at ulike forretningsregler gir riktig resultat i logger, åpne deretter for at ruting kan gå til nytt saksbehandlingssystem
        return Rute.GOSYS_OG_INFOTRYGD
            .also { log.info("Ingen av sjekkene har slått til, bruk default ruting: $it") }
    }



    /* Hjelpefunksjoner */

    internal fun erDoed(person: Person): Boolean =
        person.doedsfall.isNotEmpty()

    internal fun erKode7Fortrolig(person: Person): Boolean {
        return person.adressebeskyttelse.contains(Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG))
            .also { if (it) log.info("Personen er Kode 7 Fortrolig") }
    }

    internal fun erKode6StrengtFortrolig(person: Person): Boolean {
        return (person.adressebeskyttelse.contains(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG)) ||
                person.adressebeskyttelse.contains(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND)))
                    .also { if (it) log.info("Personen er Kode 6 Strengt fortrolig") }
    }

    internal fun erEgenAnsatt(foedselsnummer: String): Boolean {
        val request = SkjermedePersonerClient.SkjermedePersonerRequest(listOf(foedselsnummer))
        val response = skjermedePersonerClient.erSkjermet(request)
        return (response.skjermedePersonerMap[foedselsnummer] ?: false)
            .also { if (it) log.info("Personen er skjermet/egen ansatt") }
    }

    internal fun harAapenGenerellYrkesskadeSak(foedselsnummer: String): Boolean {
        val sakerForPerson = safClient.hentSakerForPerson(foedselsnummer)?.saker ?: emptyList()
        val generelleYrkesskadesaker =
            sakerForPerson.filter { sak -> sak?.tema == Tema.YRK && sak.sakstype == Sakstype.GENERELL_SAK }
        // TODO: 30/06/2022 YSMOD-408 Hva er definisjonen av "åpen"? At saken er opprettet de siste x månedene?
        return generelleYrkesskadesaker.isNotEmpty()
            .also { if (it) log.info("Personen har åpen generell sak med tema YRK") }
    }

    internal fun harEksisterendeInfotrygdSak(foedselsnumre: List<String>): Boolean {
        return infotrygdClient.harEksisterendeSak(foedselsnumre)
            .also { if (it) log.info("Personen har eksisterende Infotrygd-sak") }
    }

    /**
     * Kontrollér om det finnes journalposter for en person, som ikke har en tilknyttet sak, men som snart kan komme til
     * å få en sak i Gosys/Infotrygd.
     * OBS! Denne kontrollen må komme etter kontrollene på om det finnes saker.
     */
    internal fun harPotensiellKommendeSak(foedselsnummer: String): Boolean {
        val journalposterForPerson =
            safClient.hentJournalposterForPerson(foedselsnummer)?.dokumentoversiktBruker?.journalposter ?: emptyList()
        val journalposterUtenSak = journalposterForPerson.filter { journalpost -> journalpost?.sak == null }
        // TODO: 01/07/2022 YSMOD-375 Avstem at logikken er riktig. Bør det angis datoer i oppslaget mot saf (fraDato og tilDato)?
        return journalposterUtenSak.isNotEmpty()
            .also { if (it) log.info("Personen har potensiell kommende sak") }
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