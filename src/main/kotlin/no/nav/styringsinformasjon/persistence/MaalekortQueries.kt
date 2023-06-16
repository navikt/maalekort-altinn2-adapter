package no.nav.styringsinformasjon.persistence

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

fun DatabaseInterface.storeMaalekortXml(xml: String) {
    val insertStatement = """INSERT INTO MAALEKORT_XML (
        uuid,
        xml,
        opprettet) VALUES (?, ?, ?)
    """.trimMargin()

    val now = Timestamp.valueOf(LocalDateTime.now())
    val entryUuid = UUID.randomUUID()

    connection.use { connection ->
        connection.prepareStatement(insertStatement).use {
            it.setObject(1, entryUuid)
            it.setString(2, xml)
            it.setTimestamp(3, now)
            it.executeUpdate()
        }
        connection.commit()
    }
}

fun DatabaseInterface.fetchEveryMaalekortXml(): List<MaalekortXml> {
    val selectQuery = """SELECT *
                         FROM MAALEKORT_XML
    """.trimMargin()

    return connection.use { connection ->
        connection.prepareStatement(selectQuery).use {
            it.executeQuery().toList { toMaalekortXml() }
        }
    }
}

fun DatabaseInterface.deleteMaalekortXmlByUUID(uuid: String): Int {
    val updateStatement = """DELETE
                             FROM MAALEKORT_XML
                             WHERE uuid = ?
    """.trimIndent()

    connection.use { connection ->
        val rowsDeleted = connection.prepareStatement(updateStatement).use {
            it.setObject(1, UUID.fromString(uuid))
            it.executeUpdate()
        }

        connection.commit()
        return rowsDeleted
    }
}
