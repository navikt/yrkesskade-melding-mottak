package no.nav.yrkesskade.meldingmottak.clients.bigquery.schema

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.domene.Kanal
import no.nav.yrkesskade.model.Spraak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SchemaTest {

    @Test
    internal fun `payload transformed to skademelding_v1 row`() {
        val payload = SkademeldingPayload(
            kilde = "digital",
            tidspunktMottatt = "tidspunkt",
            spraak = Spraak.NB.toString(),
            callId = "callId"
        )

        val content = skademelding_v1.transform(jacksonObjectMapper().valueToTree(payload)).content
        assertThat(content["kilde"]).isEqualTo(payload.kilde)
        assertThat(content["tidspunktMottatt"]).isEqualTo(payload.tidspunktMottatt)
        assertThat(content["spraak"]).isEqualTo(payload.spraak)
        assertThat(content["callId"]).isEqualTo(payload.callId)
    }

    @Test
    internal fun `payload transformed to journalfoeringhendelse_oppgave_v1 row`() {
        val payload = JournalfoeringHendelseOppgavePayload(
            journalpostId = "1234",
            tittel = "skademelding",
            kanal = Kanal.NAV_NO.toString(),
            brevkode = "NAV 13",
            tildeltEnhetsnr = "4849",
            callId = "callId"
        )
        val content = journalfoeringhendelse_oppgave_v1.transform(jacksonObjectMapper().valueToTree(payload)).content
        assertThat(content["journalpostId"]).isEqualTo(payload.journalpostId)
        assertThat(content["tittel"]).isEqualTo(payload.tittel)
        assertThat(content["kanal"]).isEqualTo(payload.kanal)
        assertThat(content["brevkode"]).isEqualTo(payload.brevkode)
        assertThat(content["tildeltEnhetsnr"]).isEqualTo(payload.tildeltEnhetsnr)
        assertThat(content["callId"]).isEqualTo(payload.callId)

    }
}
