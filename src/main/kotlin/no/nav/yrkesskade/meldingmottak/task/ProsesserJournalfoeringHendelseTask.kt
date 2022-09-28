package no.nav.yrkesskade.meldingmottak.task

import com.expediagroup.graphql.generated.enums.*
import com.expediagroup.graphql.generated.journalpost.Bruker
import com.expediagroup.graphql.generated.journalpost.Journalpost
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.log.mdc.MDCConstants
import no.nav.yrkesskade.meldingmottak.clients.bigquery.BigQueryClient
import no.nav.yrkesskade.meldingmottak.clients.bigquery.schema.JournalfoeringHendelseOppgavePayload
import no.nav.yrkesskade.meldingmottak.clients.bigquery.schema.JournalfoeringHendelseRutingPayload
import no.nav.yrkesskade.meldingmottak.clients.bigquery.schema.journalfoeringhendelse_oppgave_v1
import no.nav.yrkesskade.meldingmottak.clients.bigquery.schema.ruting_v1
import no.nav.yrkesskade.meldingmottak.clients.gosys.*
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.SafClient
import no.nav.yrkesskade.meldingmottak.config.FeatureToggleService
import no.nav.yrkesskade.meldingmottak.config.FeatureToggles
import no.nav.yrkesskade.meldingmottak.domene.Brevkode
import no.nav.yrkesskade.meldingmottak.hendelser.DokumentTilSaksbehandlingClient
import no.nav.yrkesskade.meldingmottak.services.ArbeidsfordelingService
import no.nav.yrkesskade.meldingmottak.services.Rute
import no.nav.yrkesskade.meldingmottak.services.RutingResult
import no.nav.yrkesskade.meldingmottak.services.RutingService
import no.nav.yrkesskade.meldingmottak.util.FristFerdigstillelseTimeManager
import no.nav.yrkesskade.meldingmottak.util.extensions.hentBrevkode
import no.nav.yrkesskade.meldingmottak.util.extensions.hentHovedDokumentTittel
import no.nav.yrkesskade.meldingmottak.util.extensions.journalfoerendeEnhetEllerNull
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.prosessering.AsyncTaskStep
import no.nav.yrkesskade.prosessering.TaskStepBeskrivelse
import no.nav.yrkesskade.prosessering.domene.Task
import no.nav.yrkesskade.saksbehandling.model.DokumentTilSaksbehandling
import no.nav.yrkesskade.saksbehandling.model.DokumentTilSaksbehandlingHendelse
import no.nav.yrkesskade.saksbehandling.model.DokumentTilSaksbehandlingMetadata
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
    private val arbeidsfordelingService: ArbeidsfordelingService,
    private val safClient: SafClient,
    private val pdlClient: PdlClient,
    private val rutingService: RutingService,
    private val oppgaveClient: OppgaveClient,
    private val bigQueryClient: BigQueryClient,
    private val dokumentTilSaksbehandlingClient: DokumentTilSaksbehandlingClient,
    private val featureToggleService: FeatureToggleService
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

        val foedselsnummer = hentFoedselsnummer(journalpost.bruker, payloadDto.journalpostId)

        // TODO: YSMOD-509 fjerne feature toggle når ruting skal kunne gå til vår saksbehandling
        val erIkkeProd = featureToggleService.isEnabled(FeatureToggles.ER_IKKE_PROD.toggleId, false).also {
            log.info("${FeatureToggles.ER_IKKE_PROD.toggleId}: $it")
        }

        // TODO: YSMOD-509 rutingsjekken kan inlines når feature toggle er fjernet. den er trukket ut for å sikre at rutingmetrikk ble lagret til bigquery.
        val skalRutesTilYsSaksbehandling = foedselsnummer != null &&
                journalpostErKandidatForYsSaksbehandling(journalpost) &&
                skalRutesTilYsSaksbehandling(foedselsnummer, journalpost)

        if (erIkkeProd &&
            foedselsnummer != null &&
            journalpostErKandidatForYsSaksbehandling(journalpost) &&
            skalRutesTilYsSaksbehandling
        ) {
            val dokumentTilSaksbehandlingHendelse = DokumentTilSaksbehandlingHendelse(
                DokumentTilSaksbehandling(
                    journalpostId = journalpost.journalpostId,
                    enhet = arbeidsfordelingService.finnBehandlendeEnhetForPerson(foedselsnummer).enhetId
                ),
                metadata = DokumentTilSaksbehandlingMetadata(callId = MDC.get(MDCConstants.MDC_CALL_ID))
            )
            dokumentTilSaksbehandlingClient.sendTilSaksbehandling(dokumentTilSaksbehandlingHendelse).also {
                log.info("Sendt dokument til ny saksbehandlingsløsning for journalpostId ${dokumentTilSaksbehandlingHendelse.dokumentTilSaksbehandling.journalpostId}")
            }

        }
        else {
            opprettOppgave(journalpost).also { oppgave ->
                log.info("Opprettet oppgave for journalpostId ${journalpost.journalpostId}")
                foerMetrikkIBigQuery(journalpost, oppgave)
            }
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
     * Legger til en rad i BigQuery-metrikkene om hvilket saksbehandlingssystem meldingen rutes videre til.
     *
     * @param journalpost journalposten det gjelder
     * @param rutingResult hvilket system meldingen rutes videre til, samt årsak til rutingen
     */
    private fun foerMetrikkIBigQuery(journalpost: Journalpost, rutingResult: RutingResult) {
        val payload = JournalfoeringHendelseRutingPayload(
            brevkode = journalpost.hentBrevkode(),
            journalpostId = journalpost.journalpostId,
            tilSystem = rutingResult.rute.name,
            rutingAarsak = rutingResult.status.rutingAarsak()?.name,
            callId = MDC.get(MDCConstants.MDC_CALL_ID)
        )
        bigQueryClient.insert(
            ruting_v1,
            ruting_v1.transform(jacksonObjectMapper().valueToTree(payload))
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

    /**
     * Bestemmer om en journalpost er av en type som KAN sendes til nytt saksbehandlingssystem for yrkesskade/-sykdom.
     */
    private fun journalpostErKandidatForYsSaksbehandling(journalpost: Journalpost): Boolean {
        return erTannlegeerklaering(journalpost)
    }

    /**
     * Bestemmer om en journalpost, for en person, SKAL sendes til nytt saksbehandlingssystem for yrkesskade/-sykdom.
     */
    private fun skalRutesTilYsSaksbehandling(foedselsnummer: String, journalpost: Journalpost): Boolean {
        val rutingResult = rutingService.utfoerRuting(foedselsnummer)

        return rutingResult.rute == Rute.YRKESSKADE_SAKSBEHANDLING
            .also { foerMetrikkIBigQuery(journalpost, rutingResult) }
    }

    /**
     * Bestemmer om en journalpost fra en Kafka-record er en tannlegeerklæring.
     */
    private fun erTannlegeerklaering(journalpost: Journalpost): Boolean =
        (journalpost.hentBrevkode() == Brevkode.TANNLEGEERKLAERING.kode)
            .also {
                if (it) {
                    log.info("Dette er en tannlegeerklæring, brevkode er ${journalpost.hentBrevkode()}")
                }
                else {
                    log.info("Dette er ingen tannlegeerklæring, brevkode er ${journalpost.hentBrevkode()}")
                }
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

    private fun hentFoedselsnummer(bruker: Bruker?, journalpostId: String): String? {
        if (bruker?.id.isNullOrEmpty() || bruker?.type == null) {
            return null
        }

        return when (bruker.type) {
            BrukerIdType.FNR -> bruker.id.also {
                log.info("Hentet fødselsnummer fra bruker på jounalposten")
                secureLogger.info("Hentet fødselsnummer ${bruker.id} fra bruker på journalposten")
            }
            BrukerIdType.AKTOERID -> hentFoedselsnummerFraPdl(bruker.id!!, journalpostId).also {
                log.info("Hentet fødselsnummer fra pdl")
                secureLogger.info("Hentet fødselsnummer $it fra pdl for aktørId ${bruker.id}")
            }
            else -> {
                log.info("Bruker på journalpost med id $journalpostId er ikke en person!")
                secureLogger.info("Bruker ${bruker.id} på journalpost med id $journalpostId er ikke en person!")
                throw java.lang.RuntimeException("Bruker på journalpost med id $journalpostId er ikke en person!")
            }
        }
    }

    @Throws(RuntimeException::class)
    private fun hentFoedselsnummerFraPdl(aktorId: String, journalpostId: String): String {
        val identerResult = pdlClient.hentIdenter(aktorId, listOf(IdentGruppe.FOLKEREGISTERIDENT))

        val foedselsnummer = identerResult?.hentIdenter?.identer?.filter { it.gruppe == IdentGruppe.FOLKEREGISTERIDENT }
            ?.getOrNull(0)?.ident

        if (foedselsnummer == null) {
            log.error("Fant ikke fødselsnummer for bruker på journalpost med id $journalpostId")
            secureLogger.error("Fant ikke fødselsnummer for bruker på journalpost med id $journalpostId og med aktørId $aktorId")
            throw RuntimeException("Fant ikke fødselsnummer for bruker på journalpost med id $journalpostId")
        }

        return foedselsnummer
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
        val krutkoder = KrutkodeMapping.fromBrevkode(journalpost.hentBrevkode())

        val journalfoeringOppgave = OpprettJournalfoeringOppgave(
            beskrivelse = journalpost.hentHovedDokumentTittel(),
            journalpostId = journalpost.journalpostId,
            aktoerId = aktoerId,
            tema = journalpost.tema.toString(),
            tildeltEnhetsnr = journalpost.journalfoerendeEnhetEllerNull(),
            oppgavetype = Oppgavetype.JOURNALFOERING.kortnavn,
            behandlingstema = krutkoder.behandlingstema,
            behandlingstype = krutkoder.behandlingstype,
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