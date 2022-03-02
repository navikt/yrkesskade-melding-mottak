package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.meldingmottak.services.SkademeldingService
import no.nav.yrkesskade.meldingmottak.util.kallMetodeMedCallId
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import javax.transaction.Transactional


@Component
class SkademeldingInnsendtHendelseConsumer(
    private val skademeldingService: SkademeldingService
) {

//    @KafkaListener(
//        id = "skademelding-innsendt",
//        topics = ["\${kafka.topic.skademelding-innsendt}"],
//        containerFactory = "skademeldingInnsendtHendelseListenerContainerFactory",
//        idIsGroup = false
//    )
    @Transactional
    fun listen(@Payload record: SkademeldingInnsendtHendelse) {
        kallMetodeMedCallId(record.metadata.navCallId) {
            skademeldingService.mottaSkademelding(record)
        }
    }
}