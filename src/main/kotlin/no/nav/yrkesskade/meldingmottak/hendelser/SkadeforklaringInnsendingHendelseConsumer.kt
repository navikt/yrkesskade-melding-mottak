package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.meldingmottak.integration.mottak.model.SkadeforklaringInnsendingHendelse
import no.nav.yrkesskade.meldingmottak.services.SkadeforklaringService
import no.nav.yrkesskade.meldingmottak.util.kallMetodeMedCallId
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class SkadeforklaringInnsendingHendelseConsumer(
    private val skadeforklaringService: SkadeforklaringService
) {

//    @KafkaListener(
//        id = "skadeforklaring-innsendt",
//        topics = ["\${kafka.topic.skadeforklaring-innsendt}"],
//        containerFactory = "skadeforklaringInnsendingHendelseListenerContainerFactory",
//        idIsGroup = false
//    )
    @Transactional
    fun listen(@Payload record: SkadeforklaringInnsendingHendelse) {
        kallMetodeMedCallId(record.metadata.navCallId) {
            skadeforklaringService.mottaSkadeforklaring(record)
        }
    }
}