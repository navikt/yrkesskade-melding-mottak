package no.nav.yrkesskade.meldingmottak.util.extensions

import com.expediagroup.graphql.generated.journalpost.DokumentInfo
import com.expediagroup.graphql.generated.journalpost.Journalpost
import no.nav.yrkesskade.meldingmottak.domene.Brevkode
import no.nav.yrkesskade.meldingmottak.konstanter.ENHET_YRKESSYKDOM

fun Journalpost.hentHovedDokument(): DokumentInfo? = dokumenter?.firstOrNull { it!!.brevkode != null }
fun Journalpost.hentHovedDokumentTittel(): String = hentHovedDokument()?.tittel.orEmpty()
fun Journalpost.hentBrevkode(): String  = hentHovedDokument()?.brevkode.orEmpty()

/**
 * journalfoerendeEnhet overstyrer enheten som journalføringen skal tilhøre i Oppgave.
 * Noen ganger er dette en nedlagt enhet, grunnet bruk av utdatert forside på skanningen ol.
 * Andre ganger har vi behov for å overstyre enheten for spesifikke skjemaer.
 * Dette er en enkel og grei funksjon som sikrer at vi ikke sender noen journalposter til feil enhet.
 *
 */
fun Journalpost.journalfoerendeEnhetEllerNull(): String? {
    if (hentBrevkode() == Brevkode.ARBEIDSTILSYNSMELDING_KOPI.kode) {
        return ENHET_YRKESSYKDOM
    }

    if (journalfoerendeEnhet == null) {
        return null
    }

    val nedlagteEnheter = listOf("0889", "4203", "0289", "2089", "1664", "0189", "1789", "4417", "0389", "1688", "1812")
    if (nedlagteEnheter.contains(journalfoerendeEnhet)) {
        return null
    }
    return journalfoerendeEnhet
}