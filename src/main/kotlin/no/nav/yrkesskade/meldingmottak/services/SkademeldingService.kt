package no.nav.yrkesskade.meldingmottak.services

import com.expediagroup.graphql.generated.enums.BrukerIdType
import com.expediagroup.graphql.generated.journalpost.Bruker
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.AvsenderMottaker
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.DokarkivClient
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.Dokument
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.Dokumentvariant
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.Dokumentvariantformat
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.Filtype
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.Journalposttype
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.OpprettJournalpostRequest
import no.nav.yrkesskade.meldingmottak.util.getSecureLogger
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles

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
     */
    fun mottaSkademelding(record: SkademeldingInnsendtHendelse) {
        val skademelding = record.skademelding
        val skademeldingJson = objectMapper.registerModule(JavaTimeModule()).writeValueAsString(skademelding)

        val opprettJournalpostRequest = OpprettJournalpostRequest(
            journalposttype = Journalposttype.INNGAAENDE,
            avsenderMottaker = AvsenderMottaker(
                id = skademelding.innmelder!!.norskIdentitetsnummer.toString(),
                idType = BrukerIdType.FNR
            ),
            bruker = Bruker(
                id = skademelding.skadelidt!!.norskIdentitetsnummer,
                type = BrukerIdType.FNR
            ),
            tema = "YRK",
            kanal = "NAV_NO",
            datoMottatt = record.metadata.tidspunktMottatt, // string???
            dokumenter = listOf(
                Dokument(
                    brevkode = null,
                    tittel = "ORIGINAL_JSON",
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

        dokarkivClient.journalfoerSkademelding(opprettJournalpostRequest)
    }
}
