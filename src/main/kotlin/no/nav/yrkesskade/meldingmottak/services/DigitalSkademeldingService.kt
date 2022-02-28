package no.nav.yrkesskade.meldingmottak.services

import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.journalpost.Bruker
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.DokarkivClient
import no.nav.yrkesskade.meldingmottak.hendelser.domene.AvsenderMottaker
import no.nav.yrkesskade.meldingmottak.hendelser.domene.Dokument
import no.nav.yrkesskade.meldingmottak.hendelser.domene.Dokumentvariant
import no.nav.yrkesskade.meldingmottak.hendelser.domene.Dokumentvariantformat
import no.nav.yrkesskade.meldingmottak.hendelser.domene.Filtype
import no.nav.yrkesskade.meldingmottak.hendelser.domene.Journalposttype
import no.nav.yrkesskade.meldingmottak.hendelser.domene.Kanal
import no.nav.yrkesskade.meldingmottak.hendelser.domene.OpprettJournalpostRequest
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles

private const val TEMA_YRKESSKADE = "YRK"

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
            tittel = "Melding om yrkesskade eller yrkessykdom",
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
                    brevkode = "NAV 13",
                    tittel = "Melding om yrkesskade eller yrkessykdom",
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
