package no.nav.yrkesskade.meldingmottak.services

import com.expediagroup.graphql.generated.enums.*
import com.expediagroup.graphql.generated.hentperson.Adressebeskyttelse
import com.expediagroup.graphql.generated.hentperson.Person
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.SafClient
import no.nav.yrkesskade.meldingmottak.clients.infotrygd.InfotrygdClient
import no.nav.yrkesskade.meldingmottak.clients.tilgang.SkjermedePersonerClient
import no.nav.yrkesskade.meldingmottak.clients.tilgang.SkjermetPersonRequest
import no.nav.yrkesskade.meldingmottak.domene.Brevkode
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.meldingmottak.util.ruting.Enhetsruting
import no.nav.yrkesskade.skademelding.model.Skademelding
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

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
    fun utfoerRuting(foedselsnummer: String): RutingResult {
        check(foedselsnummer.isNotBlank()) { "Det må angis et fødselsnummer for å utføre ruting!" }

        val status = RutingStatus()

        val rute = ruting(foedselsnummer, status)

        return RutingResult(rute, status)
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
        val request = SkjermetPersonRequest(foedselsnummer)
        return skjermedePersonerClient.erSkjermet(request)
            .also { status.egenAnsatt = it }
    }

    internal fun harAapenGenerellYrkesskadeSak(foedselsnummer: String, status: RutingStatus): Boolean {
        val sakerForPerson = safClient.hentSakerForPerson(foedselsnummer)?.saker ?: emptyList()
        val generelleYrkesskadesaker =
            sakerForPerson.filter { sak ->
                sak?.tema == Tema.YRK &&
                        sak.sakstype == Sakstype.GENERELL_SAK &&
                        (sak.datoOpprettet == null || sak.datoOpprettet.isAfter(tjueFireMndSiden()))
            }
        return generelleYrkesskadesaker.isNotEmpty()
            .also { status.aapenGenerellYrkesskadeSak = it }
    }

    internal fun harEksisterendeInfotrygdSak(foedselsnumre: List<String>, status: RutingStatus): Boolean {
        return infotrygdClient.harEksisterendeSak(foedselsnumre)
            .also { status.eksisterendeInfotrygdSak = it }
    }

    /**
     * Kontrollér om det finnes journalposter for en person, som ikke har en tilknyttet sak, men som snart kan komme til
     * å få en sak i Gosys/Infotrygd.
     * OBS! Denne kontrollen må komme etter kontrollene på om det finnes saker.
     */
    internal fun harPotensiellKommendeSak(foedselsnummer: String, status: RutingStatus): Boolean {
        val brevkoderUnntattPotensiellKommendeSak = listOf(Brevkode.TANNLEGEERKLAERING.kode)
        val journalposterForPerson =
            safClient.hentJournalposterForPerson(
                foedselsnummer,
                listOf(Journalstatus.MOTTATT, Journalstatus.UNDER_ARBEID)
            )?.dokumentoversiktBruker?.journalposter ?: emptyList()
        val journalposterUtenSak = journalposterForPerson.filter { journalpost ->
            journalpost?.sak == null &&
            journalpost?.datoOpprettet?.isAfter(tjueFireMndSiden()) == true &&
            journalpost.dokumenter?.find { dokumentInfo ->
                brevkoderUnntattPotensiellKommendeSak.contains(dokumentInfo?.brevkode)
            } == null
        }
        return journalposterUtenSak.isNotEmpty()
            .also { status.potensiellKommendeSak = it }
    }

    internal fun hentFoedselsnumreMedHistorikk(foedselsnummer: String): List<String> {
        val identerMedHistorikk = pdlClient.hentIdenter(foedselsnummer, listOf(IdentGruppe.FOLKEREGISTERIDENT), true)

        return identerMedHistorikk?.hentIdenter?.identer?.map { it.ident }?.toList() ?: listOf(foedselsnummer)
    }

    private fun tjueFireMndSiden() = LocalDateTime.now(ZoneId.of("Europe/Oslo")).minusMonths(24)


    fun finnEnhet(skademelding: Skademelding, rutingStatus: RutingStatus = RutingStatus()): String? {
        val person = pdlClient.hentPerson(skademelding.skadelidt.norskIdentitetsnummer) ?: return null
        erKode6StrengtFortrolig(person, rutingStatus)
        return Enhetsruting.utledEnhet(skademelding, rutingStatus)
    }
}


data class RutingResult(
    val rute: Rute,
    val status: RutingStatus
)

enum class Rute {
    GOSYS_OG_INFOTRYGD,
    YRKESSKADE_SAKSBEHANDLING
}

enum class RutingAarsak {
    FINNES_IKKE_I_PDL,
    DOED,
    KODE_7_FORTROLIG,
    KODE_6_STRENGT_FORTROLIG,
    EGEN_ANSATT,
    AAPEN_GENERELL_YRKESSKADESAK,
    EKSISTERENDE_INFOTRYGDSAK,
    POTENSIELL_KOMMENDE_SAK
}

class RutingStatus {

    var finnesIkkeIPdl: Boolean = false
        set(value) {
            field = value
            oppdaterRutingResult()
        }

    var doed: Boolean = false
        set(value) {
            field = value
            oppdaterRutingResult()
        }

    var kode7Fortrolig: Boolean = false
        set(value) {
            field = value
            oppdaterRutingResult()
        }

    var kode6StrengtFortrolig: Boolean = false
        set(value) {
            field = value
            oppdaterRutingResult()
        }

    var egenAnsatt: Boolean = false
        set(value) {
            field = value
            oppdaterRutingResult()
        }

    var aapenGenerellYrkesskadeSak: Boolean = false
        set(value) {
            field = value
            oppdaterRutingResult()
        }

    var eksisterendeInfotrygdSak: Boolean = false
        set(value) {
            field = value
            oppdaterRutingResult()
        }

    var potensiellKommendeSak: Boolean = false
        set(value) {
            field = value
            oppdaterRutingResult()
        }

    var rutingResult: Rute = Rute.YRKESSKADE_SAKSBEHANDLING

    constructor(
        finnesIkkeIPdl: Boolean = false,
        doed: Boolean = false,
        kode7Fortrolig: Boolean = false,
        kode6StrengtFortrolig: Boolean = false,
        egenAnsatt: Boolean = false,
        aapenGenerellYrkesskadeSak: Boolean = false,
        eksisterendeInfotrygdSak: Boolean = false,
        potensiellKommendeSak: Boolean = false,
        rutingResult: Rute = Rute.YRKESSKADE_SAKSBEHANDLING
    ) {
        this.finnesIkkeIPdl = finnesIkkeIPdl
        this.doed = doed
        this.kode7Fortrolig = kode7Fortrolig
        this.kode6StrengtFortrolig = kode6StrengtFortrolig
        this.egenAnsatt = egenAnsatt
        this.aapenGenerellYrkesskadeSak = aapenGenerellYrkesskadeSak
        this.eksisterendeInfotrygdSak = eksisterendeInfotrygdSak
        this.potensiellKommendeSak = potensiellKommendeSak
        this.rutingResult = rutingResult
    }


    private fun oppdaterRutingResult() {
        rutingResult = if (enSjekkHarSlaattTil()) {
            Rute.GOSYS_OG_INFOTRYGD
        } else {
            Rute.YRKESSKADE_SAKSBEHANDLING
        }
    }

    fun rutingAarsak(): RutingAarsak? {
        if (finnesIkkeIPdl) {
            return RutingAarsak.FINNES_IKKE_I_PDL
        }
        if (doed) {
            return RutingAarsak.DOED
        }
        if (kode7Fortrolig) {
            return RutingAarsak.KODE_7_FORTROLIG
        }
        if (kode6StrengtFortrolig) {
            return RutingAarsak.KODE_6_STRENGT_FORTROLIG
        }
        if (egenAnsatt) {
            return RutingAarsak.EGEN_ANSATT
        }
        if (aapenGenerellYrkesskadeSak) {
            return RutingAarsak.AAPEN_GENERELL_YRKESSKADESAK
        }
        if (eksisterendeInfotrygdSak) {
            return RutingAarsak.EKSISTERENDE_INFOTRYGDSAK
        }
        if (potensiellKommendeSak) {
            return RutingAarsak.POTENSIELL_KOMMENDE_SAK
        }
        return null
    }

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
