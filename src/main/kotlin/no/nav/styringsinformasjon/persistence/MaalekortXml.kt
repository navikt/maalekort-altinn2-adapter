package no.nav.styringsinformasjon.persistence

import java.sql.ResultSet
import java.time.LocalDateTime

data class MaalekortXml(
    val uuid: String,
    val xml: String,
    val opprettet: LocalDateTime
)

