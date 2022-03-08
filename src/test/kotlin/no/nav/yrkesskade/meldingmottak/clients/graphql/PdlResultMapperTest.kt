package no.nav.yrkesskade.meldingmottak.clients.graphql

import com.expediagroup.graphql.generated.HentAdresse
import com.expediagroup.graphql.generated.HentPerson
import com.expediagroup.graphql.generated.hentperson.Bostedsadresse
import no.nav.yrkesskade.meldingmottak.fixtures.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PdlResultMapperTest {

    @Test
    fun `skal mappe personresult til navn`() {
        val navn = tilNavn(personResultMedVegadresse())
        assertThat(navn?.fornavn).isEqualTo("Ola")
        assertThat(navn?.mellomnavn).isNull()
        assertThat(navn?.etternavn).isEqualTo("Normann")
    }

    @Test
    fun `skal hente ut matrikkelId fra vegadresse`() {
        val matrikkelId = extractMatrikkelId(personResultMedVegadresse())
        assertThat(matrikkelId).isEqualTo("12345")
    }

    @Test
    fun `skal hente ut matrikkelId fra matrikkeladresse`() {
        val matrikkelId = extractMatrikkelId(personResultMedMatrikkeladresse())
        assertThat(matrikkelId).isEqualTo("6789")
    }

    @Test
    fun `skal mappe adresseresult til vegadresse`() {
        val adresse = tilAdresse(adresseResultMedVegadresse())
        assertThat(adresse?.adresselinje1).isEqualTo("Storgata 123B")
        assertThat(adresse?.adresselinje2).isEqualTo("2250 Plassen")
        assertThat(adresse?.adresselinje3).isEqualTo("Tillegg")
        assertThat(adresse?.land).isEqualTo("")
    }

    @Test
    fun `skal mappe adresseresult til matrikkeladresse`() {
        val adresse = tilAdresse(adresseResultMedMatrikkeladresse())
        assertThat(adresse?.adresselinje1).isEqualTo("Tilleggsnavn")
        assertThat(adresse?.adresselinje2).isEqualTo("5700 Kollen")
        assertThat(adresse?.adresselinje3).isEqualTo("")
        assertThat(adresse?.land).isEqualTo("")
    }

    @Test
    fun `skal mappe personresult med ukjent bosted til adresse`() {
        val ukjentBostedsadresse: Bostedsadresse = personResultMedUkjentBosted().hentPerson?.bostedsadresse?.get(0)!!
        val adresse = tilAdresse(ukjentBostedsadresse.ukjentBosted!!)
        assertThat(adresse?.adresselinje1).isEqualTo("Oslo")
        assertThat(adresse?.adresselinje2).isEqualTo("")
        assertThat(adresse?.adresselinje3).isEqualTo("")
        assertThat(adresse?.land).isEqualTo("")
    }

    @Test
    fun `skal mappe personresult med enkel utenlandsk adresse`() {
        val utenlandskBostedsadresse: Bostedsadresse = personResultMedEnkelUtenlandskAdresse().hentPerson?.bostedsadresse?.get(0)!!
        val adresse = tilAdresse(utenlandskBostedsadresse.utenlandskAdresse!!)
        assertThat(adresse?.adresselinje1).isEqualTo("Box 150")
        assertThat(adresse?.adresselinje2).isEqualTo("SE-125 24 Älvsjö")
        assertThat(adresse?.adresselinje3).isNull()
        assertThat(adresse?.land).isEqualTo("SWE")
    }

    @Test
    fun `skal mappe personresult med utenlandsk adresse`() {
        val utenlandskBostedsadresse: Bostedsadresse = personResultMedUtenlandskAdresse().hentPerson?.bostedsadresse?.get(0)!!
        val adresse = tilAdresse(utenlandskBostedsadresse.utenlandskAdresse!!)
        assertThat(adresse?.adresselinje1).isEqualTo("Glasfibergatan 10, 4. etg, Box 150")
        assertThat(adresse?.adresselinje2).isEqualTo("SE-125 24 Älvsjö")
        assertThat(adresse?.adresselinje3).isEqualTo("Stockholms län")
        assertThat(adresse?.land).isEqualTo("SWE")
    }


    private fun personResultMedVegadresse(): HentPerson.Result {
        return okResponsPersonFraPdl().data!!
    }

    private fun personResultMedMatrikkeladresse(): HentPerson.Result {
        return okResponsPersonMedMatrikkeladresseFraPdl().data!!
    }

    private fun personResultMedUkjentBosted(): HentPerson.Result {
        return okResponsPersonMedUkjentBostedFraPdl().data!!
    }

    private fun personResultMedEnkelUtenlandskAdresse(): HentPerson.Result {
        return okResponsPersonMedEnkelUtenlandskAdresseFraPdl().data!!
    }

    private fun personResultMedUtenlandskAdresse(): HentPerson.Result {
        return okResponsPersonMedUtenlandskAdresseFraPdl().data!!
    }

    private fun adresseResultMedVegadresse(): HentAdresse.Result {
        return okResponsAdresseFraPdl().data!!
    }

    private fun adresseResultMedMatrikkeladresse(): HentAdresse.Result {
        return okResponsMatrikkeladresseFraPdl().data!!
    }

}