package no.nav.yrkesskade.meldingmottak.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.DokarkivClient
import no.nav.yrkesskade.meldingmottak.domene.AvsenderMottaker
import no.nav.yrkesskade.meldingmottak.domene.Bruker
import no.nav.yrkesskade.meldingmottak.domene.BrukerIdType
import no.nav.yrkesskade.meldingmottak.domene.Dokument
import no.nav.yrkesskade.meldingmottak.domene.Dokumentvariant
import no.nav.yrkesskade.meldingmottak.domene.Dokumentvariantformat
import no.nav.yrkesskade.meldingmottak.domene.Filtype
import no.nav.yrkesskade.meldingmottak.domene.Journalposttype
import no.nav.yrkesskade.meldingmottak.domene.Kanal
import no.nav.yrkesskade.meldingmottak.domene.OpprettJournalpostRequest
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles

private const val TEMA_YRKESSKADE = "YRK"

private const val DIGITAL_SKADEMELDING_TITTEL = "Melding om yrkesskade eller yrkessykdom"

private const val DIGITAL_SKADEMELDING_BREVKODE = "NAV 13"

@Service
class SkademeldingService(
    private val dokarkivClient: DokarkivClient,
    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
) {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val secureLogger = getSecureLogger()

    /**
     * lag PDF
     * journalfør i dokarkiv
     * registrer metrikker?
     */
    fun mottaSkademelding(record: SkademeldingInnsendtHendelse) {
        val opprettJournalpostRequest = mapSkademeldingTilOpprettJournalpostRequest(record)
        dokarkivClient.journalfoerSkademelding(opprettJournalpostRequest)
    }

    private fun mapSkademeldingTilOpprettJournalpostRequest(
        record: SkademeldingInnsendtHendelse
    ): OpprettJournalpostRequest {
        val skademelding = record.skademelding
        val skademeldingJson = objectMapper.writeValueAsString(skademelding)

        return OpprettJournalpostRequest(
            tittel = DIGITAL_SKADEMELDING_TITTEL,
            journalposttype = Journalposttype.INNGAAENDE,
            avsenderMottaker = AvsenderMottaker(
                id = skademelding.innmelder!!.norskIdentitetsnummer,
                idType = BrukerIdType.FNR
            ),
            bruker = Bruker(
                id = skademelding.skadelidt!!.norskIdentitetsnummer,
                type = BrukerIdType.FNR
            ),
            tema = TEMA_YRKESSKADE,
            kanal = Kanal.NAV_NO.toString(),
            datoMottatt = record.metadata.tidspunktMottatt.toString(),
            dokumenter = listOf(
                Dokument(
                    brevkode = DIGITAL_SKADEMELDING_BREVKODE,
                    tittel = DIGITAL_SKADEMELDING_TITTEL,
                    dokumentvarianter = listOf(
                        Dokumentvariant(
                            filtype = Filtype.JSON,
                            variantformat = Dokumentvariantformat.ORIGINAL,
                            fysiskDokument = skademeldingJson.encodeToByteArray()
                        )
                    )
                )
            )
        )
    }
}
