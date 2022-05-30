package no.nav.yrkesskade.meldingmottak.services

import no.nav.yrkesskade.storage.Blob
import no.nav.yrkesskade.storage.StorageProvider
import no.nav.yrkesskade.storage.StorageType
import no.nav.yrkesskade.storage.Store
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class StorageService(@Value("\${storage.type:MEMORY}") val storageType: String) {

    val storage: Store

    init {
        storage = StorageProvider.getStorage(StorageType.valueOf(storageType))
    }

    fun hent(id: String, brukerIdentifikator: String): Blob? =
        storage.getBlob(
            Blob(
                id = id,
                bruker = brukerIdentifikator,
                null,
                null,
                null
            )
        )

}