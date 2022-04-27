package no.nav.yrkesskade.meldingmottak.util

import no.nav.yrkesskade.meldingmottak.domene.Filtype
import org.apache.tika.Tika
import org.springframework.http.MediaType
import java.util.*

internal object VedleggUtil {

    fun mediaType(bytes: ByteArray?): MediaType? {
        return Optional.ofNullable(bytes)
            .filter { b: ByteArray -> b.size > 0 }
            .map { b: ByteArray? -> MediaType.valueOf(Tika().detect(b)) }
            .orElse(null)
    }

    fun utledFiltype(bytes: ByteArray?): Filtype? {
        return when (mediaType(bytes)) {
            MediaType.APPLICATION_JSON -> Filtype.JSON
            MediaType.APPLICATION_PDF -> Filtype.PDF
            MediaType.APPLICATION_XML -> Filtype.XML
            MediaType.IMAGE_JPEG -> Filtype.JPEG
            MediaType.IMAGE_PNG -> Filtype.PNG
            MediaType.TEXT_PLAIN -> Filtype.RTF
            else -> null
        }
    }
}