package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
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
                    if (journalpost.bruker?.type?.toString().equals("AKTOERID")) {

                        // TODO hvis FNR så hent aktørid fra PDL

                        oppgaveClient.opprettOppgave(
                            OpprettJournalfoeringOppgave(
                                tittel = "Tester YRK, Hei og hopp",
                                journalpostId = journalpost.journalpostId,
                                aktoerId = journalpost.bruker?.id,
                                tema = journalpost.tema.toString(),
                                behandlingstema = BEHANDLINGSTEMA_SKADEMELDING,
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
    }

    companion object {
        private const val BEHANDLINGSTEMA_SKADEMELDING = "ab0106"
        private const val BEHANDLINGSTYPE_YRKESSKADEMELDING = "ae0045"
        private const val OPPGAVETYPE_JOURNALFOERING = "JFR"
    }
}