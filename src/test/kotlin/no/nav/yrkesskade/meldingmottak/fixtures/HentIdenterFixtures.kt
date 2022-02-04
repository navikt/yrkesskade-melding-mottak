package no.nav.yrkesskade.meldingmottak.fixtures

import com.expediagroup.graphql.generated.HentIdenter
import com.expediagroup.graphql.generated.enums.IdentGruppe
import com.expediagroup.graphql.generated.hentidenter.IdentInformasjon
import com.expediagroup.graphql.generated.hentidenter.Identliste

fun hentIdenterResultMedBrukerAktoerid(): HentIdenter.Result {
    return HentIdenter.Result(gyldigIdentlisteMedAktorId())
}

fun gyldigIdentlisteMedAktorId() = Identliste(listOf(gyldigIdentInformasjonMedAktoerId()))

fun gyldigIdentInformasjonMedAktoerId(): IdentInformasjon {
    return IdentInformasjon(
        ident = "12345",
        historisk = false,
        gruppe = IdentGruppe.AKTORID
    )
}
