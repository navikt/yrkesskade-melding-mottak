package no.nav.yrkesskade.meldingmottak.models

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.domain.Skademelding
import java.time.Instant

class SkademeldingDto(val tekst: String, val nummer: Int, val bool: Boolean, val dato: Instant?) {

    fun toSkademelding(): Skademelding {
        return Skademelding(
            id = null,
            json = jacksonObjectMapper().writeValueAsString(this),
            changedBy = "me",
            changedTime = Instant.now()
        )
    }
}

