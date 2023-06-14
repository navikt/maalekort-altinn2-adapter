package no.nav.styringsinformasjon.api.maalekort

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.registerMaalekortApi() {
    route("/api/v1/maalekort") {
        get {
            call.respondText("Kalte endepunktet GET /api/v1/maalekort")
        }
    }
}
