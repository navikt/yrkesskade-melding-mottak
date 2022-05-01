package no.nav.yrkesskade.meldingmottak.vedlegg

import org.apache.tika.Tika
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class ImageByteArray2PDFConverterTest {

    private var converter: Image2PDFConverter? = null

    @BeforeEach
    fun initFoerHverTest() {
        converter = Image2PDFConverter()
    }


    @Test
    fun jpeg_konverteres_til_Pdf() {
        assertThat(
            isPdf(converter!!.convert("pdf/vedlegg-3.jpeg"))
        ).isTrue
    }

    @Test
    fun png_konverteres_til_Pdf() {
        assertThat(
            isPdf(converter!!.convert("pdf/nav-logo.png"))
        ).isTrue
    }

    @Test
    fun gif_er_ikke_en_gyldig_vedleggtype() {
        assertThatThrownBy { converter!!.convert("pdf/loading.gif") }
            .isInstanceOf(AttachmentTypeUnsupportedException::class.java)
    }

    @Test
    fun pdf_endres_ikke() {
        assertThat(MediaType.valueOf(Tika().detect(converter!!.convert("pdf/vedlegg-1.pdf"))))
            .isEqualTo(MediaType.APPLICATION_PDF)
    }

    @Test
    fun kast_feil_for_ugyldig_filtype() {
        assertThatThrownBy { converter!!.convert(byteArrayOf(1, 2, 3, 4)) }
            .isInstanceOf(AttachmentTypeUnsupportedException::class.java)
    }

    @Test
    fun pdfManyPages() {
        val content = converter!!.convert("pdf/spring-framework-reference.pdf")
        assertThat(content).isNotEmpty
    }

    companion object {
        private val PDFSIGNATURE = byteArrayOf(0x25, 0x50, 0x44, 0x46)
        fun isPdf(fileContents: ByteArray): Boolean {
            return fileContents.copyOfRange(0, PDFSIGNATURE.size).contentEquals(PDFSIGNATURE)
        }
    }
}