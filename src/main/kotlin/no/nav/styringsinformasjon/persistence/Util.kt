package no.nav.styringsinformasjon.persistence

import java.sql.ResultSet

fun <T> ResultSet.toList(mapper: ResultSet.() -> T) = mutableListOf<T>().apply {
    while (next()) {
        add(mapper())
    }
}

fun ResultSet.toMaalekortXml() = MaalekortXml(
    uuid = getString("uuid"),
    xml = getString("xml"),
    opprettet = getTimestamp("opprettet").toLocalDateTime()
)
