package no.nav.styringsinformasjon.api.maalekort

import com.google.gson.Gson
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.styringsinformasjon.persistence.DatabaseInterface
import no.nav.styringsinformasjon.persistence.deleteMaalekortXmlByUUID
import no.nav.styringsinformasjon.persistence.fetchEveryMaalekortXml

val gson = Gson()

fun Routing.registerMaalekortApi(
    databaseAccess: DatabaseInterface
) {
    route("/api/v1/maalekort") {
        get {
            val maalekortList = databaseAccess.fetchEveryMaalekortXml()
            val response = gson.toJson(maalekortList.map { entry -> entry.xml })
            // maalekortList.forEach { databaseAccess.deleteMaalekortXmlByUUID(it.uuid) }
            call.respond(response)
        }
    }
}
