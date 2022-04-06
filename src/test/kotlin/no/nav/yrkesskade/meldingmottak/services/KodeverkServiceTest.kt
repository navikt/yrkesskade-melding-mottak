package no.nav.yrkesskade.meldingmottak.services

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import no.nav.yrkesskade.meldingmottak.clients.Kodeverkklient
import no.nav.yrkesskade.meldingmottak.domene.KodeverkTidData
import no.nav.yrkesskade.meldingmottak.domene.KodeverkTypeKategori
import no.nav.yrkesskade.meldingmottak.domene.KodeverkVerdi
import no.nav.yrkesskade.meldingmottak.fixtures.noenLand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import java.time.Instant

@Suppress("NonAsciiCharacters", "INTEGER_OPERATOR_RESOLVE_WILL_CHANGE", "UNCHECKED_CAST")
@ExtendWith(MockKExtension::class)
internal class KodeverkServiceTest {

    private val typeLandkoderISO2 = "landkoderISO2"
    private val kategoriBlank = ""
    private val bokmaal = "nb"
    private val keyLandkoder = KodeverkTypeKategori(typeLandkoderISO2, kategoriBlank)

    private val kodeverkklientMock: Kodeverkklient = mockk()

    private val service = KodeverkService(kodeverkklientMock)


    @BeforeEach
    fun setup() {
        every { kodeverkklientMock.hentKodeverk(any(), any(), any()) } returns noenLand()
    }


    @Test
    fun `skal hente land fra map når kodeverket finnes fra før`() {
        val map = mutableMapOf(keyLandkoder to KodeverkTidData(noenLand(), Instant.now()))
        ReflectionTestUtils.setField(service, "kodeverkMap", map)

        service.hentKodeverk(typeLandkoderISO2, kategoriBlank, bokmaal)
        verify(exactly = 0) { kodeverkklientMock.hentKodeverk(any(), any(), any()) }
    }

    @Test
    fun `skal hente land fra api når det er lenge siden kodeveerket ble hentet`() {
        val forLengeSiden = Instant.MIN
        val mapMedUtløptKodeverk = mutableMapOf(keyLandkoder to KodeverkTidData(noenLand(), forLengeSiden))
        ReflectionTestUtils.setField(service, "kodeverkMap", mapMedUtløptKodeverk)

        val map = (ReflectionTestUtils.getField(service, "kodeverkMap") as MutableMap<KodeverkTypeKategori, KodeverkTidData>)
        val kodeverkTidData = map[keyLandkoder]!!
        // Data finnes...
        assertThat(kodeverkTidData.data["NO"]).isEqualTo(KodeverkVerdi("NO", "NORGE"))
        // ...men er hentet for mer enn x minutter siden
        assertThat(kodeverkTidData.hentetTid).isBefore(Instant.now().minusSeconds(60*60))

        service.hentKodeverk(typeLandkoderISO2, kategoriBlank, bokmaal)
        verify(exactly = 1) { kodeverkklientMock.hentKodeverk(any(), any(), any()) }
    }

    @Test
    fun `skal hente land fra api når kodeverket ikke finnes fra før`() {
        val map = (ReflectionTestUtils.getField(service, "kodeverkMap") as MutableMap<KodeverkTypeKategori, KodeverkTidData>)
        val kodeverkTidData = map[keyLandkoder]
        // Data finnes ikke...
        assertThat(kodeverkTidData).isNull()

        service.hentKodeverk(typeLandkoderISO2, kategoriBlank, bokmaal)
        verify(exactly = 1) { kodeverkklientMock.hentKodeverk(any(), any(), any()) }
    }

    @Test
    fun `skal returnere tom map når kodeverket ikke finnes i api`() {
        every { kodeverkklientMock.hentKodeverk(any(), any(), any()) } returns emptyMap()

        val landKodeverk = service.hentKodeverk(typeLandkoderISO2, kategoriBlank, bokmaal)
        verify(exactly = 1) { kodeverkklientMock.hentKodeverk(any(), any(), any()) }
        assertThat(landKodeverk).isEmpty()
    }

}