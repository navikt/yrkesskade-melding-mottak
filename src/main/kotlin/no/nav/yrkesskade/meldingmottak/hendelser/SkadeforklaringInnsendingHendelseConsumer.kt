package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.meldingmottak.services.SkadeforklaringService
import no.nav.yrkesskade.meldingmottak.util.kallMetodeMedCallId
import no.nav.yrkesskade.skadeforklaring.integration.mottak.model.SkadeforklaringInnsendingHendelse
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class SkadeforklaringInnsendingHendelseConsumer(
    private val skadeforklaringService: SkadeforklaringService
) {

    @KafkaListener(
        id = "skadeforklaring-innsendt",
        topics = ["\${kafka.topic.skadeforklaring-innsendt.name}"],
        containerFactory = "skadeforklaringInnsendingHendelseListenerContainerFactory",
        idIsGroup = false,
        autoStartup = "\${kafka.topic.skadeforklaring-innsendt.auto-startup:true}"
    )
    @Transactional
    fun listen(@Payload record: SkadeforklaringInnsendingHendelse) {
        kallMetodeMedCallId(record.metadata.navCallId) {
            skadeforklaringService.mottaSkadeforklaring(record)
        }
    }
}