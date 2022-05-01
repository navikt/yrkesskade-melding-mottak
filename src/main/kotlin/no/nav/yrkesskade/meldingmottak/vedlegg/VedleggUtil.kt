package no.nav.yrkesskade.meldingmottak.vedlegg

import no.nav.yrkesskade.meldingmottak.domene.Filtype
import org.apache.tika.Tika
import org.springframework.http.MediaType
import java.util.*

internal object VedleggUtil {

    val GYLDIGE_VEDLEGG_FILTYPER: List<Filtype> = listOf(Filtype.PDF, Filtype.PDFA, Filtype.JPEG, Filtype.PNG)
    val GYLDIGE_BILDEVEDLEGG_FILTYPER: List<Filtype> = listOf(Filtype.JPEG, Filtype.PNG)

    fun gyldigVedleggFiltype(filtype: Filtype?): Boolean =
        GYLDIGE_VEDLEGG_FILTYPER.contains(filtype)

    fun gyldigBildevedleggFiltype(filtype: Filtype?): Boolean =
        GYLDIGE_BILDEVEDLEGG_FILTYPER.contains(filtype)

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
            MediaType.IMAGE_JPEG -> Filtype.JPEG
            MediaType.IMAGE_PNG -> Filtype.PNG
            else -> null
        }
    }
}