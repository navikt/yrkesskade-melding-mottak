package no.nav.yrkesskade.skadeforklaring.v2.model

import com.github.javafaker.Faker
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class SkadeforklaringFactory {

    companion object {
        val faker = Faker()
        fun enTid(): Tid {
            val tidstype = faker.options().option("Tidspunkt", "Periode")
            var tidspunkt = if (tidstype == "Tidspunkt") etGyldigTidspunktIFortiden() else null
            var periode = if (tidstype == "Periode") enGyldigPeriode() else null
            return Tid(
                tidstype = tidstype,
                tidspunkt = tidspunkt,
                periode = periode
            )
        }

        fun enGyldigPeriode(): Periode {
            return Periode(
                fra = etGyldigTidspunktIFortiden(),
                til = etGyldigTidspunktIFortiden()
            )
        }

        fun etGyldigSaksnummer() = faker.idNumber().valid()
        fun etGyldigTidspunktIFramtiden() = faker.date().future(356, TimeUnit.DAYS).toInstant()
        fun enGyldigDatoIFramtiden() = etGyldigTidspunktIFramtiden().atZone(ZoneId.systemDefault()).toLocalDate()
        fun etGyldigTidspunktIFortiden() = faker.date().past(356, TimeUnit.DAYS).toInstant()
        fun enGyldigDatoIFortiden() = etGyldigTidspunktIFortiden().atZone(ZoneId.systemDefault()).toLocalDate()
        fun enGyldigHelsepersonellOppsokt() = faker.options().option("ja", "nei")
        fun enGyldigSkalEttersendeDokumentasjon() = faker.options().option("ja", "nei", "ferdig")
        fun enSkadeforklaring() = this.enSkadeforklaring(etGyldigSaksnummer())
        fun etGyldigFravaer(): Fravaer {

            return Fravaer(
                fravaertype = faker.options().option(
                    "sykemelding",
                    "egenmelding",
                    "kombinasjonSykemeldingEgenmelding",
                    "alternativenePasserIkke"
                ),
                foerteDinSkadeEllerSykdomTilFravaer = faker.options()
                    .option("nei", "treDagerEllerMindre", "merEnnTreDager", "fravaersdagerUkjent", "ikkeRelevant")
            )
        }

        fun enInnmelder(): Innmelder {
            return Innmelder(
                norskIdentitetsnummer = etGyldigNorskFoedselsnummer(),
                innmelderrolle = enInnmelderrolle()
            )
        }

        fun enInnmelderrolle() = faker.options().option("denSkadelidte", "vergeOgForesatt")

        fun enSkadeforklaring(saksnummer: String?): Skadeforklaring {
            val erHelsepersonellOppsoekt = enGyldigHelsepersonellOppsokt()
            return Skadeforklaring(
                saksnummer = saksnummer,
                foersteHelsepersonellOppsoktDato = enGyldigDatoIFortiden(),
                erHelsepersonellOppsokt = erHelsepersonellOppsoekt,
                skalEttersendeDokumentasjon = enGyldigSkalEttersendeDokumentasjon(),
                tid = enTid(),
                fravaer = etGyldigFravaer(),
                arbeidetMedIUlykkesoeyeblikket = enArbeidetMedIUlykkesOeyeblikketTekst(),
                helseinstitusjoner = if (erHelsepersonellOppsoekt == "ja") noenHelseinstitusjoner() else emptyList(),
                innmelder = enInnmelder(),
                noeyaktigBeskrivelseAvHendelsen = enNoyaktigVeskrivelseAvHendelsen(),
                skadelidt = enSkadelidt(),
                vedleggreferanser = noenVedleggReferanser()
            )
        }


        fun enArbeidetMedIUlykkesOeyeblikketTekst() = faker.lorem().characters(0, 1000)

        fun noenHelseinstitusjoner(antall: Int = 1): List<Helseinstitusjon> {
            val helseinstitusjoner = ArrayList<Helseinstitusjon>(antall)
            var i = 0
            while (i < antall) {
                helseinstitusjoner.add(enHelseInstitusjon())
                i++
            }
            return helseinstitusjoner
        }

        fun enHelseInstitusjon(): Helseinstitusjon {
            return Helseinstitusjon(
                navn = "${faker.lordOfTheRings().location()} Sykehus"
            )
        }

        fun noenVedleggReferanser(): List<Vedleggreferanse> {
            return listOf(
                Vedleggreferanse(
                    id = UUID.randomUUID().toString(),
                    storrelse = faker.number().numberBetween(0, 8000).toLong(),
                    navn = faker.file().fileName(),
                    url = faker.internet().url()
                )
            )
        }

        fun enSkadelidt(): Skadelidt {
            return Skadelidt(
                norskIdentitetsnummer = etGyldigNorskFoedselsnummer()
            )
        }

        fun enNoyaktigVeskrivelseAvHendelsen() = faker.lorem().characters(0, 2000)


        fun etGyldigNorskFoedselsnummer(): String {
            return faker.options().option("27051726079","07064640911","02079543589","06015341791","28060075788","14084047776","22010166121","11012741423","21026422989","10040723493","08099429993","08088202415","02011002512","06054030914","23107913688","04053422263","26124241218","10031404318","08013448893","30076095902","23104947603","25067511754","21092328756","27107190037","18011417007","10069535610","25041331618","12060443090","22055134719","21062131052","06118312005","18124421493","19123515121","25113601011","11050090126","03067936034","22090641305","17095232108","10090813582","05098939158","14120924819","19021123024","10116218602","10059402817","04038498296","08102209452","16085520672","10093823791","02060721702","09048894051")
        }

        fun Skadeforklaring.medSaksnummer(saksnummer: String?) = this.copy(saksnummer = saksnummer)
        fun Skadeforklaring.medInnmeldersNorskIdentitetsnummer(norskIdentitetsnummer: String): Skadeforklaring {
            val innmelder = this.innmelder.copy(norskIdentitetsnummer = norskIdentitetsnummer)
            return this.copy(innmelder = innmelder)
        }

        fun Skadeforklaring.medSkadelidtsNorskIdentitetsnummer(norskIdentitetsnummer: String): Skadeforklaring {
            val skadelidt = this.skadelidt.copy(norskIdentitetsnummer = norskIdentitetsnummer)
            return this.copy(skadelidt = skadelidt)
        }

        fun Skadeforklaring.medFravaerstype(fravaertype: String): Skadeforklaring {
            val fravaer = this.fravaer.copy(fravaertype = fravaertype)
            return this.copy(fravaer = fravaer)
        }

        fun Skadeforklaring.medFoerteDinSkadeEllerSykdomTilFravaer(foerteDinSkadeEllerSykdomTilFravaer: String): Skadeforklaring {
            val fravaer = this.fravaer.copy(foerteDinSkadeEllerSykdomTilFravaer = foerteDinSkadeEllerSykdomTilFravaer)
            return this.copy(fravaer = fravaer)
        }
    }
}