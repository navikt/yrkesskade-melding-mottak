package no.nav.yrkesskade.meldingmottak.services

import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.enums.IdentGruppe
import com.expediagroup.graphql.generated.journalpost.Bruker
import com.expediagroup.graphql.generated.journalpost.Journalpost
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.Oppgavetype
import no.nav.yrkesskade.meldingmottak.clients.graphql.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.graphql.SafClient
import no.nav.yrkesskade.meldingmottak.domene.Brevkode
import no.nav.yrkesskade.meldingmottak.domene.Journalpoststatus
import no.nav.yrkesskade.meldingmottak.domene.Kanal
import no.nav.yrkesskade.meldingmottak.konstanter.TEMA_YRKESSKADE
import no.nav.yrkesskade.meldingmottak.task.ProsesserJournalfoeringHendelseTask
import no.nav.yrkesskade.meldingmottak.util.extensions.hentBrevkode
import no.nav.yrkesskade.meldingmottak.util.extensions.hentHovedDokumentTittel
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles


@Suppress("DuplicatedCode")
@Service
class JournalfoeringHendelseService(
    private val taskRepository: TaskRepository,
    private val oppgaveClient: OppgaveClient,
    private val safClient: SafClient,
    private val pdlClient: PdlClient,
    val rutingService: RutingService
) {

    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val secureLogger = getSecureLogger()

    /**
     * Tar imot en journalføringhendelse og lager en Task for opprettelse av journalføringsoppgave.
     * Betingelser for opprettelse av task:
     * 1. Temaet må tilhøre yrkesskade (YRK)
     * 2. Journalpoststatus må være en av de vi lytter på (se enum [Journalpoststatus])
     * 3. Mottakskanalen må være en av de vi lytter på (se enum [Kanal])
     * 4. Det må ikke eksistere en journalføringsoppgave på journalposten fra før
     *
     * @param record Recorden som kommer fra Kafka-topicet
     */
    fun prosesserJournalfoeringHendelse(record: JournalfoeringHendelseRecord) {
        if (!record.temaNytt.equals(TEMA_YRKESSKADE)) {
            return
        }

        if (!journalpoststatusErRelevant(record.journalpostStatus)) {
            return
        }

        if (kanalErRelevant(record)) {
            log.info("Mottatt relevant journalføringhendelse på journalpostId: ${record.journalpostId}")
            secureLogger.info("Mottatt relevant journalføringhendelse: $record")

            val eksisterendeOppgaver = oppgaveClient.finnOppgaver(record.journalpostId.toString(), Oppgavetype.JOURNALFOERING)
            if (eksisterendeOppgaver.antallTreffTotalt > 0) {
                log.warn("Det eksisterer allerede en oppgave på journalpostId ${record.journalpostId}; oppretter ikke oppgave.")
                return
            }

            val journalpost = hentJournalpostFraSaf(record.journalpostId.toString())
            val foedselsnummer = hentFoedselsnummer(journalpost.bruker, record.journalpostId.toString())

            if (journalpostSkalTilNySaksbehandling(journalpost) && rutingService.utfoerRuting(foedselsnummer) == RutingService.Rute.YRKESSKADE_SAKSBEHANDLING) {
                // TODO: legg melding på kafka
                log.info("===|> TODO: Legg melding på kafka og send til nytt saksbehandlingssystem, Kompys...")
                log.info("MIDLERTIDIG: Oppretter task og senere oppgave for behandling i gammelt saksbehandlingssystem, Gosys og Infotrygd")
            }
//            else {
                taskRepository.save(ProsesserJournalfoeringHendelseTask.opprettTask(record.journalpostId.toString()))
                log.info("Opprettet ProsesserJournalfoeringHendelseTask på journalpostId ${record.journalpostId}")
//            }
        } else {
            secureLogger.warn("Mottatt journalføringhendelse på tema YRK med ukjent kanal: $record")
        }
    }

    /**
     * Bestemmer om en Kafka-record har relevant journalpoststatus for prosessering hos oss.
     * Mottakskanalen må med andre ord være en av de vi lytter på (se enum [Journalpoststatus])
     */
    private fun journalpoststatusErRelevant(journalpostStatus: String) =
        Journalpoststatus.values()
            .map { it.toString() }
            .contains(journalpostStatus)

    /**
     * Bestemmer om en Kafka-record er på relevant kanal for prosessering hos oss.
     * Mottakskanalen må med andre ord være en av de vi lytter på (se enum [Kanal])
     *
     * @param record Recorden som kommer fra Kafka-topicet
     */
    private fun kanalErRelevant(record: JournalfoeringHendelseRecord) =
        Kanal.values()
            .map { it.toString() }
            .contains(record.mottaksKanal)

    /**
     * Bestemmer om en journalpost skal sendes til nytt saksbehandlingssystem for yrkesskade/-sykdom.
     */
    private fun journalpostSkalTilNySaksbehandling(journalpost: Journalpost): Boolean {
        return erTannlegeerklaering(journalpost)
    }

    /**
     * Bestemmer om en journalpost fra en Kafka-record er en tannlegeerklæring.
     */
    private fun erTannlegeerklaering(journalpost: Journalpost): Boolean =
        (journalpost.hentBrevkode() == Brevkode.TANNLEGEERKLAERING.kode)
            .also {
                if (it) {
                    log.info("Juhuuu, dette er en tannlegeerklæring (º-vv-º) , brevkode er ${journalpost.hentBrevkode()}")
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

    @Throws(RuntimeException::class)
    private fun hentFoedselsnummer(bruker: Bruker?, journalpostId: String): String {
        check((bruker?.id ?: "").isNotBlank()) { "Bruker på journalpost med id $journalpostId har ingen ident!" }
        check(bruker?.type != null) { "BrukerIdType på journalpost med id $journalpostId er null! " }

        return when (bruker?.type!!) {
            BrukerIdType.FNR -> bruker.id!!.also {
                    log.info("Hentet fødselsnummer fra bruker på jounalposten")
                    secureLogger.info("Hentet fødselsnummer ${bruker.id} fra bruker på journalposten")
                }
            BrukerIdType.AKTOERID -> hentFoedselsnummerFraPdl(bruker.id!!, journalpostId).also {
                    log.info("Hentet fødselsnummer fra pdl")
                    secureLogger.info("Hentet fødselsnummer $it fra pdl for aktørId ${bruker.id}")
                }
            else -> {
                throw java.lang.RuntimeException("Bruker på journalpost med id $journalpostId er ikke en person! ")
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

}
