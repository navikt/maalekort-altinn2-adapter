package no.nav.styringsinformasjon

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

const val localPropertiesPath = "./src/main/resources/localEnv.json"
val objectMapper = ObjectMapper().registerKotlinModule()

fun getEnv(): Environment {
    if (isLocal()) {
        return getLocalEnv()
    }
    return Environment(
        kafkaBrokerServer = getEnvVar("KAFKA_BROKERS"),
        schemaRegistry = KafkaSchemaRegistryEnv(
            url = getEnvVar("KAFKA_SCHEMA_REGISTRY"),
            username = getEnvVar("KAFKA_SCHEMA_REGISTRY_USER"),
            password = getEnvVar("KAFKA_SCHEMA_REGISTRY_PASSWORD"),
        ),
        sslConfig = KafkaSslEnv(
            truststoreLocation = getEnvVar("KAFKA_TRUSTSTORE_PATH"),
            keystoreLocation = getEnvVar("KAFKA_KEYSTORE_PATH"),
            credstorePassword = getEnvVar("KAFKA_CREDSTORE_PASSWORD")
        )
    )
}

fun isLocal(): Boolean = getEnvVar("KTOR_ENV", "local") == "local"

private fun getLocalEnv() =
    objectMapper.readValue(File(localPropertiesPath), Environment::class.java)

data class Environment(
    val kafkaBrokerServer: String,
    val schemaRegistry: KafkaSchemaRegistryEnv,
    val sslConfig: KafkaSslEnv
)

data class KafkaSchemaRegistryEnv(
    val url: String,
    val username: String,
    val password: String
)

data class KafkaSslEnv(
    val truststoreLocation: String,
    val keystoreLocation: String,
    val credstorePassword: String
)
