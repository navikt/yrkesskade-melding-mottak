package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.saksbehandling.model.DokumentTilSaksbehandlingHendelse
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.util.concurrent.ListenableFuture

@Component
class DokumentTilSaksbehandlingClient(
    @Value("\${kafka.topic.dokument-til-saksbehandling}") private val topic: String,
    private val dokumentTilSaksbehandlingHendelseKafkaTemplate: KafkaTemplate<String, DokumentTilSaksbehandlingHendelse>
) {

    fun sendTilSaksbehandling(
        dokumentTilSaksbehandlingHendelse: DokumentTilSaksbehandlingHendelse
    ): DokumentTilSaksbehandlingHendelse {
        val future: ListenableFuture<SendResult<String, DokumentTilSaksbehandlingHendelse>> =
            dokumentTilSaksbehandlingHendelseKafkaTemplate.send(topic, dokumentTilSaksbehandlingHendelse)
        val resultat = future.get()
        return resultat.producerRecord.value()
    }
}