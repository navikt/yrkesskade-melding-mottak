package no.nav.yrkesskade.meldingmottak.util.extensions

import com.expediagroup.graphql.generated.journalpost.DokumentInfo
import com.expediagroup.graphql.generated.journalpost.Journalpost

fun Journalpost.hentHovedDokument(): DokumentInfo? = dokumenter?.firstOrNull { it!!.brevkode != null }
fun Journalpost.hentHovedDokumentTittel(): String = hentHovedDokument()?.tittel.orEmpty()
fun Journalpost.hentBrevkode(): String  = hentHovedDokument()?.brevkode.orEmpty()

/**
 * journalfoerendeEnhet overstyrer enheten som journalføringen skal tilhøre i Oppgave.
 * Noen ganger er dette en nedlagt enhet, grunnet bruk av utdatert forside på skanningen ol.
 * Dette er en midlertidig funksjon vi bruker for å sørge for at vi ikke sender nedlagt enhet til Oppgave-APIet.
 *
 */
fun Journalpost.journalfoerendeEnhetEllerNull(): String? {
    if (journalfoerendeEnhet == null) {
        return null
    }

    val nedlagteEnheter = listOf("0889", "4203", "0289", "2089", "1664", "0189", "1789", "4417", "0389", "1688")
    if (nedlagteEnheter.contains(journalfoerendeEnhet)) {
        return null
    }
    return journalfoerendeEnhet
}