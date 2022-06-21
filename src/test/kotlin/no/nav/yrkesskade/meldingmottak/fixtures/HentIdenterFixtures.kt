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

fun hentIdenterResultMedFnrHistorikk(): HentIdenter.Result {
    return HentIdenter.Result(gyldigIdentlisteMedFnrHistorikk())
}

fun gyldigIdentlisteMedFnrHistorikk() =
    Identliste(
        listOf(
            identInformasjon_1_Dnr(),
            identInformasjon_2_GammeltFnr(),
            identInformasjon_3_NyttFnr()
        )
    )

fun identInformasjon_1_Dnr(): IdentInformasjon {
    return IdentInformasjon(
        ident = "11111111111",
        historisk = true,
        gruppe = IdentGruppe.FOLKEREGISTERIDENT
    )
}

fun identInformasjon_2_GammeltFnr(): IdentInformasjon {
    return IdentInformasjon(
        ident = "22222222222",
        historisk = true,
        gruppe = IdentGruppe.FOLKEREGISTERIDENT
    )
}

fun identInformasjon_3_NyttFnr(): IdentInformasjon {
    return IdentInformasjon(
        ident = "33333333333",
        historisk = false,
        gruppe = IdentGruppe.FOLKEREGISTERIDENT
    )
}


