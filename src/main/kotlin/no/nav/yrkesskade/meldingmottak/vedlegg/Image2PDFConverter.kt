package no.nav.yrkesskade.meldingmottak.vedlegg

import no.nav.yrkesskade.meldingmottak.vedlegg.ImageScaler.downToA4
import no.nav.yrkesskade.meldingmottak.vedlegg.VedleggUtil.mediaType
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.function.Consumer

@Component
class Image2PDFConverter private constructor(private val supportedMediaTypes: List<MediaType>) {

//    @Inject
    constructor() : this(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG)

    internal constructor(vararg supportedMediaTypes: MediaType) : this(supportedMediaTypes.toList())

    fun convert(classPathResource: String): ByteArray {
        return try {
            convert(ClassPathResource(classPathResource))
        } catch (e: AttachmentException) {
            throw e
        } catch (e: Exception) {
            throw AttachmentConversionException("Kunne ikke konvertere vedlegg $classPathResource", e)
        }
    }

    @Throws(IOException::class)
    fun convert(res: Resource): ByteArray {
        return convert(StreamUtils.copyToByteArray(res.inputStream))
    }

    fun convert(bytes: ByteArray): ByteArray {
        val mediaType: MediaType? = mediaType(bytes)
        if (MediaType.APPLICATION_PDF == mediaType) {
            return bytes
        }
        if (!validImageTypes(mediaType)) {
            throw AttachmentTypeUnsupportedException(mediaType)
        }
        return embedImagesInPdf(mediaType!!.subtype, bytes)
    }

    private fun validImageTypes(mediaType: MediaType?): Boolean {
        val isValid = supportedMediaTypes.contains(mediaType)
        LOG.info("{} konvertere bytes av type {} til PDF", if (isValid) "Vil" else "Vil ikke", mediaType)
        return isValid
    }

    override fun toString(): String {
        return javaClass.simpleName + " [supportedMediaTypes=" + supportedMediaTypes + "]"
    }



    companion object {
        private val LOG = LoggerFactory.getLogger(Image2PDFConverter::class.java)

        private fun embedImagesInPdf(imgType: String, vararg images: ByteArray): ByteArray {
            return embedImagesInPdf(images.toList(), imgType)
        }

        private fun embedImagesInPdf(images: List<ByteArray>, imgType: String): ByteArray {
            try {
                PDDocument().use { doc ->
                    ByteArrayOutputStream().use { outputStream ->
                        images.forEach(Consumer { i: ByteArray -> addPDFPageFromImage(doc, i, imgType) })
                        doc.save(outputStream)
                        return outputStream.toByteArray()
                    }
                }
            } catch (e: Exception) {
                throw AttachmentConversionException("Konvertering av vedlegg feilet", e)
            }
        }

        private fun addPDFPageFromImage(doc: PDDocument, orig: ByteArray, fmt: String) {
            val page = PDPage(PDRectangle.A4)
            doc.addPage(page)
            try {
                PDPageContentStream(doc, page).use { cs ->
                    cs.drawImage(
                        PDImageXObject.createFromByteArray(
                            doc,
                            downToA4(orig, fmt),
                            "img"
                        ),
                        PDRectangle.A4.lowerLeftX,
                        PDRectangle.A4.lowerLeftY
                    )
                }
            } catch (e: Exception) {
                throw AttachmentConversionException("Konvertering av vedlegg feilet", e)
            }
        }
    }
}