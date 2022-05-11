package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.meldingmottak.task.ProsesserDigitalSkademeldingTask
import no.nav.yrkesskade.meldingmottak.util.kallMetodeMedCallId
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.prosessering.domene.TaskRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import javax.transaction.Transactional


@Component
class SkademeldingInnsendtHendelseConsumer(
    private val taskRepository: TaskRepository
) {

    @KafkaListener(
        id = "skademelding-innsendt",
        topics = ["\${kafka.topic.skademelding-innsendt}"],
        containerFactory = "skademeldingInnsendtHendelseListenerContainerFactory",
        idIsGroup = false
    )
    @Transactional
    fun listen(@Payload record: SkademeldingInnsendtHendelse) {
        kallMetodeMedCallId(record.metadata.navCallId) {
            taskRepository.save(ProsesserDigitalSkademeldingTask.opprettTask(record))
        }
    }
}