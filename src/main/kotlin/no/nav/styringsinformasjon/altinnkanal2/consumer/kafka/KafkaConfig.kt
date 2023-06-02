package no.nav.styringsinformasjon.altinnkanal2.consumer.kafka

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nav.styringsinformasjon.ApplicationState
import no.nav.styringsinformasjon.Environment
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import java.time.Duration
import java.util.*

interface KafkaListener {
    suspend fun listen(applicationState: ApplicationState)
}

const val JAVA_KEYSTORE = "JKS"
const val PKCS12 = "PKCS12"
const val SSL = "SSL"
const val BASIC_AUTH_CREDENTIALS_SOURCE = "basic.auth.credentials.source"
const val USER_INFO = "USER_INFO"

const val topicAltinnMaalekortMottatt = "aapen-altinn-maalekort-mottatt-v2"

val zeroMillis = Duration.ofMillis(0L)

fun kafkaConsumerProperties(env: Environment): Properties {
    val sslConfig = env.sslConfig
    val userinfoConfig = "${env.schemaRegistry.username}:${env.schemaRegistry.password}"

    return HashMap<String, String>().apply {
        put(CommonClientConfigs.GROUP_ID_CONFIG, "maalekort-altinnkanal-2-consumer")
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1")
        put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

        put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SSL)
        put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "")
        put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, JAVA_KEYSTORE)
        put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, PKCS12)
        put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslConfig.truststoreLocation)
        put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslConfig.credstorePassword)
        put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslConfig.keystoreLocation)
        put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslConfig.credstorePassword)
        put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslConfig.credstorePassword)
        remove(SaslConfigs.SASL_MECHANISM)
        remove(SaslConfigs.SASL_JAAS_CONFIG)

        put(BASIC_AUTH_CREDENTIALS_SOURCE, USER_INFO)
        put(SchemaRegistryClientConfig.USER_INFO_CONFIG, userinfoConfig)
        put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, env.schemaRegistry.url)
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer")
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer")
        put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true")

        put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, env.kafkaBrokerServer)
    }.toProperties()
}

suspend fun CoroutineScope.launchKafkaListener(applicationState: ApplicationState, kafkaListener: KafkaListener) {
    launch {
        try {
            kafkaListener.listen(applicationState)
        } finally {
            applicationState.running = false
        }
    }
}
