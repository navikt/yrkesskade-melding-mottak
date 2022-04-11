package no.nav.yrkesskade.meldingmottak.clients.gosys

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KrutkodeMappingTest {

    @Test
    fun `UTL skal gi behandlingstema ab0276 og behandlingstype ae0106`() {
        val mapping = KrutkodeMapping.fromBrevkode("UTL")
        assertThat(mapping.behandlingstema).isEqualTo("ab0276")
        assertThat(mapping.behandlingstype).isEqualTo("ae0106")
    }

    @Test
    fun `NAVe 13 13 05 skal gi behandlingstema ab0085 og behandlingstype null`() {
        val mapping = KrutkodeMapping.fromBrevkode("NAVe 13-13.05")
        assertThat(mapping.behandlingstema).isEqualTo("ab0085")
        assertThat(mapping.behandlingstype).isNull()
    }

    @Test
    fun `NAVe 13 07 05 skal gi behandlingstema ab0276 og behandlingstype null`() {
        val mapping = KrutkodeMapping.fromBrevkode("NAVe 13-07.05")
        assertThat(mapping.behandlingstema).isEqualTo("ab0276")
        assertThat(mapping.behandlingstype).isNull()
    }

    @Test
    fun `NAV 13 13 05 skal gi behandlingstema ab0085 og behandlingstype null`() {
        val mapping = KrutkodeMapping.fromBrevkode("NAV 13-13.05")
        assertThat(mapping.behandlingstema).isEqualTo("ab0085")
        assertThat(mapping.behandlingstype).isNull()
    }

    @Test
    fun `ukjent brevkode skal gi UKJENT`() {
        val mapping = KrutkodeMapping.fromBrevkode("eksisterer ikke")
        assertThat(mapping).isEqualTo(KrutkodeMapping.UKJENT)
        assertThat(mapping.behandlingstema).isNull()
        assertThat(mapping.behandlingstype).isNull()
    }

    @Test
    fun `fromBrevkode skal haandtere null som input`() {
        val mapping = KrutkodeMapping.fromBrevkode(null)
        assertThat(mapping).isEqualTo(KrutkodeMapping.UKJENT)
        assertThat(mapping.behandlingstema).isNull()
        assertThat(mapping.behandlingstype).isNull()
    }

    @Test
    fun `fromBrevkode skal haandtere tom streng som input`() {
        val mapping = KrutkodeMapping.fromBrevkode("")
        assertThat(mapping).isEqualTo(KrutkodeMapping.UKJENT)
        assertThat(mapping.behandlingstema).isNull()
        assertThat(mapping.behandlingstype).isNull()
    }

    @Test
    fun `skal kunne matche selv om det er whitespace bak brevkoden`() {
        val mapping = KrutkodeMapping.fromBrevkode("UTL ")
        assertThat(mapping).isEqualTo(KrutkodeMapping.UTL)
    }

    @Test
    fun `skal kunne matche selv om det er whitespace foran brevkoden`() {
        val mapping = KrutkodeMapping.fromBrevkode(" UTL")
        assertThat(mapping).isEqualTo(KrutkodeMapping.UTL)
    }
}