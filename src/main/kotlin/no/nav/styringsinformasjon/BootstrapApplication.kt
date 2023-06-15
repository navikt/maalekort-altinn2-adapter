package no.nav.styringsinformasjon

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.ktor.server.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import no.nav.styringsinformasjon.altinnkanal2.consumer.kafka.AltinnKanal2Listener
import no.nav.styringsinformasjon.altinnkanal2.consumer.kafka.ReceivedMessageProcessor
import no.nav.styringsinformasjon.altinnkanal2.consumer.kafka.launchKafkaListener
import no.nav.styringsinformasjon.api.maalekort.registerMaalekortApi
import no.nav.styringsinformasjon.api.registerNaisApi
import no.nav.styringsinformasjon.api.registerPrometheusApi
import no.nav.styringsinformasjon.persistence.DatabaseInterface
import no.nav.styringsinformasjon.persistence.Database
import no.nav.styringsinformasjon.persistence.grantAccessToIAMUsers
import no.nav.styringsinformasjon.service.MaalekortService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class ApplicationState(var running: Boolean = false, var initialized: Boolean = false)

val state: ApplicationState = ApplicationState()
val backgroundTasksContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
lateinit var database: DatabaseInterface

fun main() {
    val env = getEnv()

    val server = embeddedServer(
        Netty,
        applicationEngineEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load())
            database = Database(env.databaseConnectionConfig)
            database.grantAccessToIAMUsers()

            val maalekortService = MaalekortService(database)

            connector {
                port = getEnvVar("APPLICATION_PORT", "8080").toInt()
            }

            module {
                state.running = true
                serverModule()
                kafkaModule(
                    env,
                    listOf(maalekortService)
                )
            }
        }
    )

    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop(10, 10, TimeUnit.SECONDS)
        }
    )
    server.start(wait = false)
}

fun Application.serverModule() {

    install(ContentNegotiation) {
        jackson {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    routing {
        registerPrometheusApi()
        registerNaisApi(state)
        registerMaalekortApi()
    }

    state.initialized = true
}

fun Application.kafkaModule(env: Environment, messageProcessors: List<ReceivedMessageProcessor>) {
    messageProcessors.forEach { messageProcessor ->
        runningRemotely {
            launch(backgroundTasksContext) {
                launchKafkaListener(
                    state,
                    AltinnKanal2Listener(env, messageProcessor)
                )
            }
        }
    }
}

val Application.envKind
    get() = environment.config.property("ktor.environment").getString()
fun Application.runningRemotely(block: () -> Unit) {
    if (envKind == "remote") block()
}

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
