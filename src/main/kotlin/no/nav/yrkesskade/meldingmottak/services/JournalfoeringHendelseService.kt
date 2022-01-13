package no.nav.yrkesskade.meldingmottak.services

import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.enums.Journalstatus
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.journalpost.Bruker
import com.expediagroup.graphql.generated.journalpost.Journalpost
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.clients.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.SafClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OpprettJournalfoeringOppgave
import no.nav.yrkesskade.meldingmottak.clients.gosys.Prioritet
import no.nav.yrkesskade.meldingmottak.util.FristFerdigstillelseTimeManager
import no.nav.yrkesskade.meldingmottak.util.extensions.hentHovedDokumentTittel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles
import java.time.LocalDate
import java.time.LocalDateTime

private const val OPPGAVETYPE_JOURNALFOERING = "JFR"

@Service
class JournalfoeringHendelseService(
    private val safClient: SafClient,
    private val pdlClient: PdlClient,
    private val oppgaveClient: OppgaveClient
) {

    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    fun prosesserJournalfoeringHendelse(record: JournalfoeringHendelseRecord) {
        val journalpost = hentJournalpostFraSaf(record)
        log.info("Oppdatert journalpost for journalpostId ${record.journalpostId}: $journalpost")

        val aktoerId = hentAktoerId(journalpost.bruker!!)

        oppgaveClient.opprettOppgave(
            OpprettJournalfoeringOppgave(
                beskrivelse = journalpost.hentHovedDokumentTittel(),
                journalpostId = journalpost.journalpostId,
                aktoerId = aktoerId,
                tema = journalpost.tema.toString(),
                oppgavetype = OPPGAVETYPE_JOURNALFOERING,
                behandlingstema = null, // skal være null
                behandlingstype = null, // skal være null
                prioritet = Prioritet.NORM,
                fristFerdigstillelse = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(LocalDateTime.now()),
                aktivDato = LocalDate.now()
            )
        )
    }

    @Throws(RuntimeException::class)
    private fun hentJournalpostFraSaf(record: JournalfoeringHendelseRecord): Journalpost {
        val safResultat = safClient.hentOppdatertJournalpost(record.journalpostId.toString())
        if (safResultat?.journalpost == null) {
            log.error("Fant ikke journalpost i SAF for journalpostId ${record.journalpostId}")
            throw RuntimeException("Journalpost med journalpostId ${record.journalpostId} finnes ikke i SAF")
        }
        validerJournalpost(safResultat.journalpost)

        return safResultat.journalpost
    }

    private fun hentAktoerId(bruker: Bruker): String? {
        return when (bruker.type) {
            BrukerIdType.AKTOERID -> bruker.id
            BrukerIdType.FNR -> pdlClient.hentAktorId(bruker.id!!)
            else -> throw RuntimeException("Ugyldig brukerIdType: ${bruker.type}")
        }
    }

    private fun validerJournalpost(journalpost: Journalpost) {
        log.info("Validerer journalpost fra SAF med journalpostId ${journalpost.journalpostId}")

        if (journalpost.journalstatus != Journalstatus.MOTTATT) {
            throw RuntimeException("Journalstatus må være ${Journalstatus.MOTTATT}, men er: ${journalpost.journalstatus}")
        }

        if (journalpost.tema != Tema.YRK) {
            throw RuntimeException("Journalpostens tema må være ${Tema.YRK}, men er: ${journalpost.tema}")
        }

        if (journalpost.dokumenter.isNullOrEmpty()) {
            throw RuntimeException("Journalposten mangler dokumenter.")
        }

        if (journalpost.bruker?.id.isNullOrEmpty()) {
            throw RuntimeException("Journalposten mangler brukerId.")
        }

        val gyldigeBrukerIdTyper = listOf(BrukerIdType.FNR, BrukerIdType.AKTOERID)
        if (!gyldigeBrukerIdTyper.contains(journalpost.bruker?.type)) {
            throw RuntimeException("BrukerIdType må være en av: $gyldigeBrukerIdTyper, men er: ${journalpost.bruker?.type}")
        }
    }
}