package no.nav.yrkesskade.meldingmottak.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.yrkesskade.meldingmottak.clients.dokarkiv.DokarkivClient
import no.nav.yrkesskade.meldingmottak.domene.OpprettJournalpostResponse
import no.nav.yrkesskade.meldingmottak.fixtures.skademeldingInnsendtHendelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SkademeldingServiceMockTest {

    private val dokarkivClient: DokarkivClient = mockk()

    private val service: SkademeldingService = SkademeldingService(dokarkivClient)

    @BeforeEach
    fun setup() {
        every { dokarkivClient.journalfoerSkademelding(any()) } answers { OpprettJournalpostResponse(false, "123", emptyList()) }
    }

    @Test
    fun `skal kalle paa taskRepository naar en record med tema YRK og kanal SKAN_IM kommer inn`() {
        service.mottaSkademelding(skademeldingInnsendtHendelse())
        verify(exactly = 1) { dokarkivClient.journalfoerSkademelding(any()) }
    }
}