package no.nav.yrkesskade.meldingmottak.vedlegg

import no.nav.yrkesskade.meldingmottak.domene.Filtype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class VedleggUtilTest {

    @Test
    fun `er gyldig vedleggtype`() {
        assertThat(VedleggUtil.gyldigVedleggFiltype(Filtype.PDF)).isTrue
        assertThat(VedleggUtil.gyldigVedleggFiltype(Filtype.PDFA)).isTrue
        assertThat(VedleggUtil.gyldigVedleggFiltype(Filtype.JPEG)).isTrue
        assertThat(VedleggUtil.gyldigVedleggFiltype(Filtype.PNG)).isTrue
    }

    @Test
    fun `er ikke gyldig vedleggtype`() {
        assertThat(VedleggUtil.gyldigVedleggFiltype(Filtype.JSON)).isFalse
    }

    @Test
    fun `er gyldig bildevedleggtype`() {
        assertThat(VedleggUtil.gyldigBildevedleggFiltype(Filtype.JPEG)).isTrue
        assertThat(VedleggUtil.gyldigBildevedleggFiltype(Filtype.PNG)).isTrue
    }

    @Test
    fun `er ikke gyldig bildevedleggtype`() {
        assertThat(VedleggUtil.gyldigBildevedleggFiltype(Filtype.JSON)).isFalse
        assertThat(VedleggUtil.gyldigBildevedleggFiltype(Filtype.PDF)).isFalse
        assertThat(VedleggUtil.gyldigBildevedleggFiltype(Filtype.PDFA)).isFalse
    }

    @Test
    fun `skal utlede filtype av filinnholdet`() {
        assertThat(VedleggUtil.utledFiltype(readPdfFile(), "fil1")).isEqualTo(Filtype.PDF)
        assertThat(VedleggUtil.utledFiltype(readJpegFile(), "fil2")).isEqualTo(Filtype.JPEG)
        assertThat(VedleggUtil.utledFiltype(readPngFile(), "fil3")).isEqualTo(Filtype.PNG)
    }

    @Test
    fun `skal utlede filtype av filendelse dersom ikke allerede utledet av filinnholdet`() {
        assertThat(VedleggUtil.utledFiltype(byteArrayOf(), "dok1.JSON")).isEqualTo(Filtype.JSON)
        assertThat(VedleggUtil.utledFiltype(byteArrayOf(), "test.pdf")).isEqualTo(Filtype.PDF)
        assertThat(VedleggUtil.utledFiltype(byteArrayOf(), "bilde.jPeG")).isEqualTo(Filtype.JPEG)
        assertThat(VedleggUtil.utledFiltype(byteArrayOf(), "screenshot.18.Jpg")).isEqualTo(Filtype.JPEG)
        assertThat(VedleggUtil.utledFiltype(byteArrayOf(), "image.png")).isEqualTo(Filtype.PNG)
    }

    @Test
    fun `ignorer "vedlegg mangler" ved utleding av filtypen`() {
        assertThat(VedleggUtil.utledFiltype(byteArrayOf(), "vedlegg1.jpeg - VEDLEGG MANGLER, KONTAKT INNMELDER")).isEqualTo(Filtype.JPEG)
    }

    private fun readPdfFile(): ByteArray =
        ClassPathResource("pdf/vedlegg-1.pdf").file.readBytes()

    private fun readJpegFile(): ByteArray =
        ClassPathResource("pdf/nav-logo.jpeg").file.readBytes()

    private fun readPngFile(): ByteArray =
        ClassPathResource("pdf/nav-logo.png").file.readBytes()

}