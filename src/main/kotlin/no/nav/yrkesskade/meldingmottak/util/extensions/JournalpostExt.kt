package no.nav.yrkesskade.meldingmottak.util.extensions

import com.expediagroup.graphql.generated.journalpost.Journalpost

fun Journalpost.hentHovedDokumentTittel(): String {
    return dokumenter?.firstOrNull { it!!.brevkode != null }?.tittel.orEmpty()
}