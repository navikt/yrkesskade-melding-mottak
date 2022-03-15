package no.nav.yrkesskade.meldingmottak.task

import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.enums.Journalposttype
import com.expediagroup.graphql.generated.enums.Journalstatus
import com.expediagroup.graphql.generated.enums.Tema
import com.expediagroup.graphql.generated.journalpost.Bruker
import com.expediagroup.graphql.generated.journalpost.Journalpost
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.log.mdc.MDCConstants
import no.nav.yrkesskade.meldingmottak.clients.bigquery.BigQueryClient
import no.nav.yrkesskade.meldingmottak.clients.bigquery.schema.JournalfoeringHendelseOppgavePayload
import no.nav.yrkesskade.meldingmottak.clients.bigquery.schema.journalfoeringhendelse_oppgave_v1
import no.nav.yrkesskade.meldingmottak.clients.gosys.Oppgave
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.Oppgavetype
import no.nav.yrkesskade.meldingmottak.clients.gosys.OpprettJournalfoeringOppgave
import no.nav.yrkesskade.meldingmottak.clients.gosys.Prioritet
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.SafClient
import no.nav.yrkesskade.meldingmottak.util.FristFerdigstillelseTimeManager
import no.nav.yrkesskade.meldingmottak.util.extensions.hentBrevkode
import no.nav.yrkesskade.meldingmottak.util.extensions.hentHovedDokumentTittel
import no.nav.yrkesskade.meldingmottak.util.extensions.journalfoerendeEnhetEllerNull
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.prosessering.AsyncTaskStep
import no.nav.yrkesskade.prosessering.TaskStepBeskrivelse
import no.nav.yrkesskade.prosessering.domene.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.lang.invoke.MethodHandles


@TaskStepBeskrivelse(
    taskStepType = ProsesserJournalfoeringHendelseTask.TASK_STEP_TYPE,
    beskrivelse = "Prosessering av skannet og journalført skademelding",
    maxAntallFeil = 10,
    triggerTidVedFeilISekunder = 60 * 30
)
@Component
class ProsesserJournalfoeringHendelseTask(
    private val safClient: SafClient,
    private val pdlClient: PdlClient,
    private val oppgaveClient: OppgaveClient,
    private val bigQueryClient: BigQueryClient
) : AsyncTaskStep {

    val log: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val secureLogger = getSecureLogger()

    override fun doTask(task: Task) {
        log.info("ProsesserJournalfoeringHendelseTask kjoerer med payload ${task.payload}")
        val payloadDto = jacksonObjectMapper().readValue<ProsesserJournalfoeringHendelseTaskPayloadDto>(task.payload)

        val journalpost = hentJournalpostFraSaf(payloadDto.journalpostId)

        if (!journalpostErRelevant(journalpost)) {
            return
        }
        validerJournalpost(journalpost)

        opprettOppgave(journalpost).also { oppgave ->
            log.info("Opprettet oppgave for journalpostId ${journalpost.journalpostId}")
            foerMetrikkIBigQuery(journalpost, oppgave)
        }
    }

    /**
     * Legger til en rad i BigQuery-metrikkene om at en journalfoeringsoppgave er opprettet.
     *
     * @param journalpost journalposten det gjelder
     * @param oppgave oppgaven som ble opprettet
     */
    private fun foerMetrikkIBigQuery(journalpost: Journalpost, oppgave: Oppgave) {
        val payload = JournalfoeringHendelseOppgavePayload(
            journalpostId = journalpost.journalpostId,
            tittel = journalpost.hentHovedDokumentTittel(),
            kanal = journalpost.kanal.toString(),
            brevkode = journalpost.hentBrevkode(),
            behandlingstema = journalpost.behandlingstema.orEmpty(),
            enhetFraJournalpost = journalpost.journalfoerendeEnhet.orEmpty(),
            tildeltEnhetsnr = oppgave.tildeltEnhetsnr,
            manglerNorskIdentitetsnummer = journalpost.bruker?.id.isNullOrEmpty(),
            callId = MDC.get(MDCConstants.MDC_CALL_ID)
        )
        bigQueryClient.insert(
            journalfoeringhendelse_oppgave_v1,
            journalfoeringhendelse_oppgave_v1.transform(jacksonObjectMapper().valueToTree(payload))
        )
    }

    /**
     * Avgjør om en journalpost er relevant for opprettelse av journalføringsoppgave.
     * Kriterier:
     * 1. Journalpostens status må være mottatt
     * 2. Journalpostens tema må være YRK
     * 3. Journalpostens type på være I (innkommende)
     *
     * @param journalpost journalposten som skal vurderes
     */
    private fun journalpostErRelevant(journalpost: Journalpost): Boolean {
        log.info("Sjekker om journalpost med journalpostId ${journalpost.journalpostId} er relevant for oppgaveopprettelse")
        if (journalpost.journalstatus != Journalstatus.MOTTATT) {
            log.warn("Journalstatus må være ${Journalstatus.MOTTATT}, men er: ${journalpost.journalstatus}")
            return false
        }

        if (journalpost.tema != Tema.YRK) {
            log.warn("Journalpostens tema må være ${Tema.YRK}, men er: ${journalpost.tema}")
            return false
        }

        if (journalpost.journalposttype != Journalposttype.I) {
            log.warn("Journalpostens type må være ${Journalposttype.I}, men er: ${journalpost.journalposttype}")
            return false
        }
        return true
    }

    @Throws(RuntimeException::class)
    private fun hentJournalpostFraSaf(journalpostId: String): Journalpost {
        val safResultat = safClient.hentOppdatertJournalpost(journalpostId)
        if (safResultat?.journalpost == null) {
            log.error("Fant ikke journalpost i SAF for journalpostId $journalpostId")
            throw RuntimeException("Journalpost med journalpostId $journalpostId finnes ikke i SAF")
        }

        return safResultat.journalpost.also {
            secureLogger.info(
                "Hentet oppdatert journalpost med id $journalpostId" +
                        ", kanal ${it.kanal}" +
                        ", tittel \"${it.hentHovedDokumentTittel()}\"" +
                        ", journalstatus ${it.journalstatus}" +
                        ", journalposttype ${it.journalposttype}" +
                        ", tema ${it.tema}" +
                        ", journalfoerendeEnhet ${it.journalfoerendeEnhet}" +
                        ", behandlingstema ${it.behandlingstema}" +
                        ", datoOpprettet ${it.datoOpprettet}"
            )
        }
    }

    private fun hentAktoerId(bruker: Bruker?): String? {
        if (bruker?.id.isNullOrEmpty()) {
            log.warn("Journalposten har ingen brukerId.")
            return null
        }
        return when (bruker!!.type) {
            BrukerIdType.AKTOERID -> bruker.id
            BrukerIdType.FNR -> pdlClient.hentAktorId(bruker.id!!)
            else -> throw RuntimeException("Ugyldig brukerIdType: ${bruker.type}")
        }
    }

    /**
     * Avgjør om en journalpost er gyldig (inneholder data som vi kan jobbe med)
     * Kriterier:
     * 1. Det må foreligge dokumenter på journalposten
     * 2. BrukerId må være fødselsnummer/D-nummer, eller aktørId (kan ikke være orgnr)
     *
     * @param journalpost journalposten som skal vurderes
     */
    private fun validerJournalpost(journalpost: Journalpost) {
        log.info("Validerer journalpost fra SAF med journalpostId ${journalpost.journalpostId}")

        if (journalpost.dokumenter.isNullOrEmpty()) {
            throw RuntimeException("Journalposten mangler dokumenter.")
        }

        val gyldigeBrukerIdTyper = listOf(BrukerIdType.FNR, BrukerIdType.AKTOERID)
        if (!journalpost.bruker?.id.isNullOrEmpty() && !gyldigeBrukerIdTyper.contains(journalpost.bruker?.type)) {
            throw RuntimeException("BrukerIdType må være en av: $gyldigeBrukerIdTyper, men er: ${journalpost.bruker?.type}")
        }
    }

    fun opprettOppgave(journalpost: Journalpost): Oppgave {
        val aktoerId = hentAktoerId(journalpost.bruker)

        val journalfoeringOppgave = OpprettJournalfoeringOppgave(
            beskrivelse = journalpost.hentHovedDokumentTittel(),
            journalpostId = journalpost.journalpostId,
            aktoerId = aktoerId,
            tema = journalpost.tema.toString(),
            tildeltEnhetsnr = journalpost.journalfoerendeEnhetEllerNull(),
            oppgavetype = Oppgavetype.JOURNALFOERING.kortnavn,
            behandlingstema = null, // skal være null
            behandlingstype = null, // skal være null
            prioritet = Prioritet.NORM,
            fristFerdigstillelse = FristFerdigstillelseTimeManager.nesteGyldigeFristForFerdigstillelse(journalpost.datoOpprettet),
            aktivDato = journalpost.datoOpprettet.toLocalDate()
        )

        return oppgaveClient.opprettOppgave(journalfoeringOppgave)
    }

    companion object {
        fun opprettTask(journalpostId: String): Task {
            return Task(
                type = TASK_STEP_TYPE,
                payload = jacksonObjectMapper().writeValueAsString(
                    ProsesserJournalfoeringHendelseTaskPayloadDto(journalpostId)
                )
            )
        }

        const val TASK_STEP_TYPE = "ProsesserJournalfoeringHendelse"
    }
}

data class ProsesserJournalfoeringHendelseTaskPayloadDto(val journalpostId: String)