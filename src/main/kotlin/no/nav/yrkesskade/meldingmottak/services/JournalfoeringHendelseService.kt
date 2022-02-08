package no.nav.yrkesskade.meldingmottak.services

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.hendelser.domene.Kanal
import no.nav.yrkesskade.meldingmottak.task.ProsesserJournalfoertSkanningTask
import no.nav.yrkesskade.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles

private const val TEMA_YRKESSKADE = "YRK"

@Service
class JournalfoeringHendelseService(private val taskRepository: TaskRepository) {

    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    /**
     * Tar imot en journalføringhendelse og lager en Task for opprettelse av journalføringsoppgave.
     * Betingelser for opprettelse av task:
     * 1. Temaet må tilhøre yrkesskade (YRK)
     * 2. Mottakskanalen må være en av de vi lytter på (se enum [Kanal])
     *
     * @param record Recorden som kommer fra Kafka-topicet
     */
    fun prosesserJournalfoeringHendelse(record: JournalfoeringHendelseRecord) {
        if (!record.temaNytt.equals(TEMA_YRKESSKADE)) {
            return
        }

        if (kanalErRelevant(record)) {
            log.info("Mottatt journalføringhendelse, oppretter journalføringsoppgave: $record")
            taskRepository.save(ProsesserJournalfoertSkanningTask.opprettTask(record.journalpostId.toString()))
        } else {
            log.warn("Mottatt journalføringhendelse på tema YRK med ukjent kanal: $record")
        }
    }

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
}
