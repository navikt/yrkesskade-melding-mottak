package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.meldingmottak.task.ProsesserSkadeforklaringTask
import no.nav.yrkesskade.meldingmottak.util.kallMetodeMedCallId
import no.nav.yrkesskade.prosessering.domene.TaskRepository
import no.nav.yrkesskade.skadeforklaring.v1.integration.model.SkadeforklaringInnsendingHendelse as SkadeforklaringInnsendingHendelseV1
import no.nav.yrkesskade.skadeforklaring.v2.integration.model.SkadeforklaringInnsendingHendelse as SkadeforklaringInnsendingHendelseV2
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
@KafkaListener(
    id = "skadeforklaring-innsendt",
    topics = ["\${kafka.topic.skadeforklaring-innsendt}"],
    containerFactory = "skadeforklaringInnsendingHendelseListenerContainerFactory",
    idIsGroup = false
)
class SkadeforklaringInnsendingHendelseConsumer(
    private val taskRepository: TaskRepository,
    private val prosesserSkadeforklaringTask: ProsesserSkadeforklaringTask
) {

    @Transactional
    @KafkaHandler(isDefault = true)
    fun handleSkadeforklaringV1(record: SkadeforklaringInnsendingHendelseV1) {
        kallMetodeMedCallId(record.metadata.navCallId) {
            taskRepository.save(prosesserSkadeforklaringTask.opprettTask(record))
        }
    }

    @Transactional
    @KafkaHandler()
    fun handleSkadeforklaringV2(record: SkadeforklaringInnsendingHendelseV2) {
        kallMetodeMedCallId(record.metadata.navCallId) {
            taskRepository.save(prosesserSkadeforklaringTask.opprettTask(record))
        }
    }
}