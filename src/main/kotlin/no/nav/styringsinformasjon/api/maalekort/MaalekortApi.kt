package no.nav.styringsinformasjon.api.maalekort

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.styringsinformasjon.persistence.DatabaseInterface
import no.nav.styringsinformasjon.persistence.deleteMaalekortXmlByUUID
import no.nav.styringsinformasjon.persistence.fetchEveryMaalekortXml
import java.util.*

const val deleteRequestHeader = "maalekort-uuid"
const val errorMessageMissingHeader = "Mangler '$deleteRequestHeader' i header på request"
private fun errorMessageNotFound(uuid: String) = "Fant ikke målekort med $uuid"

fun Routing.registerMaalekortApi(
    databaseAccess: DatabaseInterface
) {
    route("/api/v1/maalekort") {
        authenticate("basic-auth") {
            get {
                call.respond(databaseAccess.fetchEveryMaalekortXml().map { entry -> mapOf(entry.uuid to entry.xml) })
            }

            delete {
                val listOfMaalekortToDelete = validateUuidHeader(call.request.headers["maalekort-uuid"])
                listOfMaalekortToDelete?.map { uuid ->
                    val maalekortToDelete = uuid.toString()
                    val rowsDeleted = databaseAccess.deleteMaalekortXmlByUUID(maalekortToDelete)
                    if (rowsDeleted ==  0) {
                        call.respond(
                            status = HttpStatusCode.NotFound,
                            message = errorMessageNotFound(maalekortToDelete)
                        )
                    }
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = "Slettet $rowsDeleted målekort fra databasen"
                    )
                }
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = errorMessageMissingHeader
                )
            }
        }
    }
}

private fun validateUuidHeader(header: String?): List<UUID>? {
    return header?.let {
        val headerUuids = header.split(",")
        return@let try {
            headerUuids.map { uuid -> UUID.fromString(uuid) }
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
