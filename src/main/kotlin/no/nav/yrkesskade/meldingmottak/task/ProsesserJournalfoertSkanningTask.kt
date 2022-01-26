package no.nav.yrkesskade.meldingmottak.task

import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.enums.Journalposttype
import com.expediagroup.graphql.generated.enums.Journalstatus
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.journalpost.Bruker
import com.expediagroup.graphql.generated.journalpost.Journalpost
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.yrkesskade.meldingmottak.clients.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.SafClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OpprettJournalfoeringOppgave
import no.nav.yrkesskade.meldingmottak.clients.gosys.Prioritet
import no.nav.yrkesskade.meldingmottak.util.FristFerdigstillelseTimeManager
import no.nav.yrkesskade.meldingmottak.util.extensions.hentHovedDokumentTittel
import no.nav.yrkesskade.prosessering.AsyncTaskStep
import no.nav.yrkesskade.prosessering.TaskStepBeskrivelse
import no.nav.yrkesskade.prosessering.domene.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.invoke.MethodHandles
import java.time.LocalDate
import java.time.LocalDateTime

private const val OPPGAVETYPE_JOURNALFOERING = "JFR"

@TaskStepBeskrivelse(
    taskStepType = ProsesserJournalfoertSkanningTask.TASK_STEP_TYPE,
    beskrivelse = "Prosessering av skannet og journalført skademelding",
    maxAntallFeil = 10,
    triggerTidVedFeilISekunder = 60 * 30
)
@Component
class ProsesserJournalfoertSkanningTask(
    private val safClient: SafClient,
    private val pdlClient: PdlClient,
    private val oppgaveClient: OppgaveClient,
) : AsyncTaskStep {

    val log: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    override fun doTask(task: Task) {
        log.info("ProsesserJournalfoertSkanningTask kjoerer med payload ${task.payload}")
        val payloadDto = jacksonObjectMapper().readValue<ProsesserJournalfoertSkanningTaskPayloadDto>(task.payload)

        val journalpost = hentJournalpostFraSaf(payloadDto.journalpostId)
        log.info("Oppdatert journalpost for journalpostId ${payloadDto.journalpostId}: $journalpost")

        val aktoerId = hentAktoerId(journalpost.bruker!!)

        // TODO: 26/01/2022 burde fristFerdigstillelse og aktivDato settes fra tidspunktet tasken ble opprettet, og ikke tidspunktet for vellykket task-kjøring?
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
    private fun hentJournalpostFraSaf(journalpostId: String): Journalpost {
        val safResultat = safClient.hentOppdatertJournalpost(journalpostId)
        if (safResultat?.journalpost == null) {
            log.error("Fant ikke journalpost i SAF for journalpostId $journalpostId")
            throw RuntimeException("Journalpost med journalpostId $journalpostId finnes ikke i SAF")
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

        if (journalpost.journalposttype != Journalposttype.I) {
            throw RuntimeException("Journalpostens type må være ${Journalposttype.I}, men er: ${journalpost.journalposttype}")
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

    companion object {
        fun opprettTask(journalpostId: String): Task {
            return Task(
                type = TASK_STEP_TYPE,
                payload = jacksonObjectMapper().writeValueAsString(
                    ProsesserJournalfoertSkanningTaskPayloadDto(journalpostId)
                )
            )
        }

        const val TASK_STEP_TYPE = "prosesserJournalfoertSkanning"
    }
}

data class ProsesserJournalfoertSkanningTaskPayloadDto(val journalpostId: String)