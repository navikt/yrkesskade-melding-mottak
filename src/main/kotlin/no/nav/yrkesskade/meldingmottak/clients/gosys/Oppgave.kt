package no.nav.yrkesskade.meldingmottak.clients.gosys

import java.time.LocalDate
import java.time.OffsetDateTime

data class OppgaveResponse(
    val antallTreffTotalt: Int,
    val oppgaver: List<Oppgave>
)

data class Oppgave(
    val id: Long,
    val tildeltEnhetsnr: String,
    val endretAvEnhetsnr: String? = null,
    val opprettetAvEnhetsnr: String? = null,
    val journalpostId: String? = null,
    val journalpostkilde: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val saksreferanse: String? = null,
    val bnr: String? = null,
    val samhandlernr: String? = null,
    val aktoerId: String? = null,
    val identer: List<Ident>? = null,
    val orgnr: String? = null,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val temagruppe: String? = null,
    val tema: String,
    val behandlingstema: String? = null,
    val oppgavetype: String,
    val behandlingstype: String? = null,
    val versjon: Int,
    val mappeId: Long? = null,
    val opprettetAv: String,
    val endretAv: String? = null,
    val prioritet: Prioritet,
    val status: Status,
    val metadata: Map<String, String>? = null,
    val fristFerdigstillelse: LocalDate?,
    val aktivDato: LocalDate,
    val opprettetTidspunkt: OffsetDateTime,
    val ferdigstiltTidspunkt: OffsetDateTime? = null,
    val endretTidspunkt: OffsetDateTime? = null
)

// De feltene som skal kunne endres må gjøres mutable/defineres som var isf val
data class OpprettJournalfoeringOppgave(
    val beskrivelse: String? = null,
    val journalpostId: String? = null,
    val aktoerId: String? = null,
    val tema: String,
    val behandlingstema: String? = null,
    val oppgavetype: String,
    val behandlingstype: String? = null,
    val prioritet: Prioritet,
    val fristFerdigstillelse: LocalDate?,
    val aktivDato: LocalDate? = null
)

data class Ident(
    val ident: String? = null,
    val gruppe: Gruppe? = null
)

enum class Gruppe {
    FOLKEREGISTERIDENT, AKTOERID, NPID
}

enum class Prioritet {
    HOY, NORM, LAV
}

enum class Status {
    OPPRETTET, AAPNET, UNDER_BEHANDLING, FERDIGSTILT, FEILREGISTRERT;

    fun statuskategori(): Statuskategori {
        return when (this) {
            AAPNET, OPPRETTET, UNDER_BEHANDLING -> Statuskategori.AAPEN
            FEILREGISTRERT, FERDIGSTILT -> Statuskategori.AVSLUTTET
        }
    }
}

enum class Statuskategori {
    AAPEN, AVSLUTTET
}


