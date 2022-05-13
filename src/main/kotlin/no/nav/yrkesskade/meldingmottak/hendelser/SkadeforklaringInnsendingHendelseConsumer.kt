package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.meldingmottak.task.ProsesserSkadeforklaringTask
import no.nav.yrkesskade.meldingmottak.util.kallMetodeMedCallId
import no.nav.yrkesskade.prosessering.domene.TaskRepository
import no.nav.yrkesskade.skadeforklaring.integration.mottak.model.SkadeforklaringInnsendingHendelse
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class SkadeforklaringInnsendingHendelseConsumer(
    private val taskRepository: TaskRepository
) {

    @KafkaListener(
        id = "skadeforklaring-innsendt",
        topics = ["\${kafka.topic.skadeforklaring-innsendt}"],
        containerFactory = "skadeforklaringInnsendingHendelseListenerContainerFactory",
        idIsGroup = false
    )
    @Transactional
    fun listen(@Payload record: SkadeforklaringInnsendingHendelse) {
        kallMetodeMedCallId(record.metadata.navCallId) {
            taskRepository.save(ProsesserSkadeforklaringTask.opprettTask(record))
        }
    }
}