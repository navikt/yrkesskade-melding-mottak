package no.nav.yrkesskade.meldingmottak.services

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.yrkesskade.meldingmottak.task.ProsesserJournalfoertSkanningTask
import no.nav.yrkesskade.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles

private const val TEMA_YRKESSKADE = "YRK"
private const val KANALPREFIKS_SKANNING = "SKAN_"

@Service
class JournalfoeringHendelseService(private val taskRepository: TaskRepository) {

    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    fun prosesserJournalfoeringHendelse(record: JournalfoeringHendelseRecord) {
        if (hendelseErRelevant(record)) {
            log.info("Mottatt journalføringhendelse: $record")
            taskRepository.save(ProsesserJournalfoertSkanningTask.opprettTask(record.journalpostId.toString()))
        }
    }

    /**
     * Bestemmer om en Kafka-record er relevant for prosessering hos oss.
     * Betingelser:
     * 1. Temaet må tilhøre yrkesskade (YRK)
     * 2. Mottakskanalen må være skanning (starter med "SKAN_")
     *
     * @param record Recorden som kommer fra Kafka-topicet
     */
    private fun hendelseErRelevant(record: JournalfoeringHendelseRecord) =
        record.temaNytt.equals(TEMA_YRKESSKADE) && record.mottaksKanal.startsWith(KANALPREFIKS_SKANNING)
}
