package no.nav.yrkesskade.meldingmottak.services

import com.expediagroup.graphql.generated.enums.*
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

        val status = RutingStatus()

        return ruting(foedselsnummer, status)
            .also { log.info(status.resultatSomTekst()) }
    }

    private fun ruting(foedselsnummer: String, status: RutingStatus): Rute {

        val person = pdlClient.hentPerson(foedselsnummer)

        if (finnesIkkeIPdl(person, status)
            || erDoed(person!!, status)
            || erKode7Fortrolig(person, status)
            || erKode6StrengtFortrolig(person, status)
            || erEgenAnsatt(foedselsnummer, status)
            || harAapenGenerellYrkesskadeSak(foedselsnummer, status)
        ) {
            return Rute.GOSYS_OG_INFOTRYGD
        }

        val foedselsnumreMedHistorikk = hentFoedselsnumreMedHistorikk(foedselsnummer)
        if (harEksisterendeInfotrygdSak(foedselsnumreMedHistorikk, status)
            || harPotensiellKommendeSak(foedselsnummer, status)
        ) {
            return Rute.GOSYS_OG_INFOTRYGD
        }

        return Rute.YRKESSKADE_SAKSBEHANDLING
    }



    /* Hjelpefunksjoner */

    internal fun finnesIkkeIPdl(person: Person?, status: RutingStatus): Boolean =
        (person == null)
            .also { status.finnesIkkeIPdl = it }

    internal fun erDoed(person: Person, status: RutingStatus): Boolean =
        person.doedsfall.isNotEmpty()
            .also { status.doed = it }

    internal fun erKode7Fortrolig(person: Person, status: RutingStatus): Boolean {
        return person.adressebeskyttelse.contains(Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG))
            .also { status.kode7Fortrolig = it }
    }

    internal fun erKode6StrengtFortrolig(person: Person, status: RutingStatus): Boolean {
        return (person.adressebeskyttelse.contains(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG)) ||
                person.adressebeskyttelse.contains(Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND)))
                    .also { status.kode6StrengtFortrolig = it }
    }

    internal fun erEgenAnsatt(foedselsnummer: String, status: RutingStatus): Boolean {
        val request = SkjermedePersonerClient.SkjermetPersonRequest(foedselsnummer)
        return skjermedePersonerClient.erSkjermet(request)
            .also { status.egenAnsatt = it }
    }

    internal fun harAapenGenerellYrkesskadeSak(foedselsnummer: String, status: RutingStatus): Boolean {
        val sakerForPerson = safClient.hentSakerForPerson(foedselsnummer)?.saker ?: emptyList()
        val generelleYrkesskadesaker =
            sakerForPerson.filter { sak -> sak?.tema == Tema.YRK && sak.sakstype == Sakstype.GENERELL_SAK }
        // TODO: 30/06/2022 YSMOD-408 Hva er definisjonen av "åpen"? At saken er opprettet de siste x månedene?
        return generelleYrkesskadesaker.isNotEmpty()
            .also { status.aapenGenerellYrkesskadeSak = it }
    }

    internal fun harEksisterendeInfotrygdSak(foedselsnumre: List<String>, status: RutingStatus): Boolean {
        // TODO: 09/08/2022 YSMOD-459 Midlertidig utkommentert sjekk om eksisterende sak i Infotrygd
        return false
            .also { log.info("MIDLERTIDIG: HOPPER OVER SJEKK OM PERSONEN HAR EN EKSISTERENDE SAK I INFOTRYGD") }
//        return infotrygdClient.harEksisterendeSak(foedselsnumre)
//            .also { status.eksisterendeInfotrygdSak = it }
    }

    /**
     * Kontrollér om det finnes journalposter for en person, som ikke har en tilknyttet sak, men som snart kan komme til
     * å få en sak i Gosys/Infotrygd.
     * OBS! Denne kontrollen må komme etter kontrollene på om det finnes saker.
     */
    internal fun harPotensiellKommendeSak(foedselsnummer: String, status: RutingStatus): Boolean {
        val journalposterForPerson =
            safClient.hentJournalposterForPerson(
                foedselsnummer,
                listOf(Journalstatus.MOTTATT, Journalstatus.UNDER_ARBEID)
            )?.dokumentoversiktBruker?.journalposter ?: emptyList()
        val journalposterUtenSak = journalposterForPerson.filter { journalpost -> journalpost?.sak == null }
        // TODO: 01/07/2022 YSMOD-375 Avstem at logikken er riktig. Bør det angis datoer i oppslaget mot saf (fraDato og tilDato)?
        return journalposterUtenSak.isNotEmpty()
            .also { status.potensiellKommendeSak = it }
    }

    internal fun hentFoedselsnumreMedHistorikk(foedselsnummer: String): List<String> {
        val identerMedHistorikk = pdlClient.hentIdenter(foedselsnummer, listOf(IdentGruppe.FOLKEREGISTERIDENT), true)

        return identerMedHistorikk?.hentIdenter?.identer?.map { it.ident }?.toList() ?: listOf(foedselsnummer)
    }


    enum class Rute {
        GOSYS_OG_INFOTRYGD,
        YRKESSKADE_SAKSBEHANDLING
    }

    class RutingStatus (
        var finnesIkkeIPdl: Boolean = false,
        var doed: Boolean = false,
        var kode7Fortrolig: Boolean = false,
        var kode6StrengtFortrolig: Boolean = false,
        var egenAnsatt: Boolean = false,
        var aapenGenerellYrkesskadeSak: Boolean = false,
        var eksisterendeInfotrygdSak: Boolean = false,
        var potensiellKommendeSak: Boolean = false,
        var rutingResult: Rute = Rute.YRKESSKADE_SAKSBEHANDLING
    ) {

        fun resultatSomTekst(): String {
            val builder: StringBuilder = java.lang.StringBuilder("Rutingstatus for person:\n")
            builder.append("------------------------------------------\n")
            leggTilStatusLinje("Finnes ikke i PDL", finnesIkkeIPdl, builder)
            leggTilStatusLinje("Er død", doed, builder)
            leggTilStatusLinje("Er fortrolig (kode 7)", kode7Fortrolig, builder)
            leggTilStatusLinje("Er strengt fortrolig (kode 6)", kode6StrengtFortrolig, builder)
            leggTilStatusLinje("Er egen ansatt/skjermet person", egenAnsatt, builder)
            leggTilStatusLinje("Har åpen generell YRK-sak", aapenGenerellYrkesskadeSak, builder)
            leggTilStatusLinje("Har eksisterende Infotrygd-sak", eksisterendeInfotrygdSak, builder)
            leggTilStatusLinje("Har potensiell kommende sak", potensiellKommendeSak, builder)

            if (!enSjekkHarSlaattTil()) {
                builder.append("Ingen av sjekkene har slått til, bruk default ruting\n")
            }
            builder.append("Resultat:  $rutingResult")

            return builder.toString()
        }

        private fun leggTilStatusLinje(ledetekst: String, bool: Boolean, builder: StringBuilder) {
            builder.append(ledetekst + jaNeiTekst(bool, ledetekst.length) + "\n")
        }

        private fun jaNeiTekst(p: Boolean, ledetekstlengde: Int): String {
            val jaNei = if (p) "[Ja]" else "[Nei]"
            return jaNei.padStart(50 - ledetekstlengde)
        }

        private fun enSjekkHarSlaattTil(): Boolean {
            return finnesIkkeIPdl ||
                    doed ||
                    kode7Fortrolig ||
                    kode6StrengtFortrolig ||
                    egenAnsatt ||
                    aapenGenerellYrkesskadeSak ||
                    eksisterendeInfotrygdSak ||
                    potensiellKommendeSak
        }

    }

}