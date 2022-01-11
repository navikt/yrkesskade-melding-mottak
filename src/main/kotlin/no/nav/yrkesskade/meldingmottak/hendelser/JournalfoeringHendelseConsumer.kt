package no.nav.yrkesskade.meldingmottak.hendelser

import com.expediagroup.graphql.generated.enums.BrukerIdType
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.clients.PdlClient
import no.nav.yrkesskade.meldingmottak.clients.SafClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OppgaveClient
import no.nav.yrkesskade.meldingmottak.clients.gosys.OpprettJournalfoeringOppgave
import no.nav.yrkesskade.meldingmottak.clients.gosys.Prioritet
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles
import java.time.LocalDate
import javax.transaction.Transactional


@Service
class JournalfoeringHendelseConsumer(
    private val safClient: SafClient,
    private val pdlClient: PdlClient,
    private val oppgaveClient: OppgaveClient
) {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    @KafkaListener(
        id = "yrkesskade-melding-mottak",
        topics = ["\${kafka.topic.aapen-dok-journalfoering}"],
        containerFactory = "kafkaJournalfoeringHendelseListenerContainerFactory",
        idIsGroup = false
    )
    @Transactional
    fun listen(@Payload record: JournalfoeringHendelseRecord) {
        if (record.temaNytt.equals("YRK")) {
            log.info(record.toString())

            val oppdatertJournalpost = safClient.hentOppdatertJournalpost(record.journalpostId.toString())
            if (oppdatertJournalpost == null) {
                log.warn("Fant ikke journalpost i SAF for journalpostId ${record.journalpostId}")
            } else {
                log.info("Oppdatert journalpost for journalpostId ${record.journalpostId}: $oppdatertJournalpost")

                oppdatertJournalpost.journalpost?.let { journalpost ->
                    val aktoerId: String?
                    val brukerIdType = journalpost.bruker?.type ?: BrukerIdType.__UNKNOWN_VALUE

                    if (journalpost.bruker?.type?.toString().equals("AKTOERID")) {
                        aktoerId = journalpost.bruker?.id
                    } else if (brukerIdType == BrukerIdType.FNR && journalpost.bruker?.id != null) {
                        aktoerId = pdlClient.hentAktorId(journalpost.bruker.id)
                    } else {
                        log.error("Journalpost med journalpostId ${record.journalpostId} inneholder verken aktørId eller fødselsnummer! Ingen journalføringsoppgave opprettes.")
                        // TODO: 23/12/2021 Kast exception
                        throw RuntimeException("Journalpost med journalpostId ${record.journalpostId} inneholder verken aktørId eller fødselsnummer")
                    }

                    oppgaveClient.opprettOppgave(
                        OpprettJournalfoeringOppgave(
                            beskrivelse = "Tester YRK, Hei og hopp",
                            journalpostId = journalpost.journalpostId,
                            aktoerId = aktoerId,
                            tema = journalpost.tema.toString(),
                            behandlingstema = null,
                            oppgavetype = OPPGAVETYPE_JOURNALFOERING,
                            behandlingstype = null,
                            prioritet = Prioritet.NORM,
                            fristFerdigstillelse = LocalDate.now().plusDays(1),
                            aktivDato = LocalDate.now()
                        )
                    )
                }
            }
        }
    }

    companion object {
        private const val BEHANDLINGSTEMA_SKADEMELDING = "ab0106"
        private const val BEHANDLINGSTYPE_YRKESSKADEMELDING = "ae0045"
        private const val OPPGAVETYPE_JOURNALFOERING = "JFR"
    }
}