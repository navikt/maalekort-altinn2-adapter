package no.nav.styringsinformasjon.persistence

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

fun DatabaseInterface.storeMaalekortXml(xml: String) {
    val insertStatement1 = """INSERT INTO MAALEKORT_XML (
        uuid,
        xml,
        opprettet) VALUES (?, ?, ?)
    """.trimIndent()

    val now = Timestamp.valueOf(LocalDateTime.now())
    val entryUuid = UUID.randomUUID()

    connection.use { connection ->
        connection.prepareStatement(insertStatement1).use {
            it.setObject(1, entryUuid)
            it.setString(2, xml)
            it.setTimestamp(3, now)
            it.executeUpdate()
        }
        connection.commit()
    }
}
