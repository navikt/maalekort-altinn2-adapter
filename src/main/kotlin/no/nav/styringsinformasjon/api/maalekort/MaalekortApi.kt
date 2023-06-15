package no.nav.styringsinformasjon.api.maalekort

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.styringsinformasjon.persistence.DatabaseInterface
import no.nav.styringsinformasjon.persistence.deleteMaalekortXmlByUUID
import no.nav.styringsinformasjon.persistence.fetchEveryMaalekortXml

fun Routing.registerMaalekortApi(
    databaseAccess: DatabaseInterface
) {
    route("/api/v1/maalekort") {
        get {
            val maalekortList = databaseAccess.fetchEveryMaalekortXml()
            // maalekortList.forEach { databaseAccess.deleteMaalekortXmlByUUID(it.uuid) }
            call.respond(maalekortList.map { entry -> mapOf( entry.uuid to entry.xml ) })
        }
    }
}
