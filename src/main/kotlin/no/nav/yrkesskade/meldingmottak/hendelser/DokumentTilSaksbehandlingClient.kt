package no.nav.yrkesskade.meldingmottak.hendelser

import no.nav.yrkesskade.saksbehandling.model.DokumentTilSaksbehandling
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.util.concurrent.ListenableFuture

@Component
class DokumentTilSaksbehandlingClient(
    @Value("\${kafka.topic.dokument-til-saksbehandling}") private val topic: String,
    private val dokumentTilSaksbehandlingKafkaTemplate: KafkaTemplate<String, DokumentTilSaksbehandling>
) {

    fun sendTilSaksbehandling(dokumentTilSaksbehandling: DokumentTilSaksbehandling): DokumentTilSaksbehandling {
        val future: ListenableFuture<SendResult<String, DokumentTilSaksbehandling>> = dokumentTilSaksbehandlingKafkaTemplate.send(topic, dokumentTilSaksbehandling)
        val resultat = future.get()
        return resultat.producerRecord.value()
    }
}