package no.nav.styringsinformasjon.persistence

import no.nav.styringsinformasjon.DbEnv
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import org.flywaydb.core.Flyway

const val postgresJdbcPrefix = "jdbc:postgresql"

interface DatabaseInterface {
    val connection: Connection
}


class Database(val env: DbEnv) : DatabaseInterface {
    val hikariDataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = generateJdbcUrlFromEnv(env)
            username = env.dbUsername
            password = env.dbPassword
            maximumPoolSize = 2
            minimumIdle = 1
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
    )

    init {
        runFlywayMigrations(hikariDataSource)
    }

    override val connection: Connection
        get() = hikariDataSource.connection

    private fun runFlywayMigrations(hikariDataSource: HikariDataSource) =
        Flyway.configure().run {
            dataSource(hikariDataSource)
            load().migrate().migrationsExecuted
        }
}

fun generateJdbcUrlFromEnv(env: DbEnv): String {
    return "$postgresJdbcPrefix://${env.dbHost}:${env.dbPort}/${env.dbName}"
}

fun DatabaseInterface.grantAccessToIAMUsers() {
    val statement = """
        GRANT ALL ON ALL TABLES IN SCHEMA PUBLIC TO CLOUDSQLIAMUSER
    """.trimIndent()

    connection.use { conn ->
        conn.prepareStatement(statement).use {
            it.executeUpdate()
        }
        conn.commit()
    }
}
