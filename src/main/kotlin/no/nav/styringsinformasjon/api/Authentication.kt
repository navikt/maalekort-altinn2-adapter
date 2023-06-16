package no.nav.styringsinformasjon.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.nav.styringsinformasjon.Environment


fun Application.setupAuth(
    env: Environment
) {
    val username = env.auth.username
    val password = env.auth.password
    install(Authentication) {
        basic("basic-auth") {
            validate { credentials ->
                if (credentials.name == username && credentials.password == password) {
                    return@validate UserIdPrincipal(credentials.name)
                }
                return@validate null
            }
        }
    }
}