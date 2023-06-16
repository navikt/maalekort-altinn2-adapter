package no.nav.styringsinformasjon.api.maalekort

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.styringsinformasjon.persistence.DatabaseInterface
import no.nav.styringsinformasjon.persistence.deleteMaalekortXmlByUuidList
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
                listOfMaalekortToDelete?.let { uuidList ->
                    val rowsDeleted = databaseAccess.deleteMaalekortXmlByUuidList(uuidList)
                    if (rowsDeleted ==  0) {
                        call.respond(
                            status = HttpStatusCode.NotFound,
                            message = errorMessageNotFound(uuidList.joinToString(","))
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
        val headerUuids = header.trim().split(",")
        return@let try {
            headerUuids.map { uuid -> UUID.fromString(uuid.trim()) }
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
