package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles

@Service
class SkademeldingService {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val secureLogger = getSecureLogger()

    /**
     * lag PDF
     * journalfør i dokarkiv
     */
    fun mottaSkademelding(record: SkademeldingInnsendtHendelse) {
//        OpprettJournalpostRequest()
    }
}
