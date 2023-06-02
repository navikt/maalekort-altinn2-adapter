package no.nav.styringsinformasjon.altinnkanal2.consumer.kafka

import no.nav.altinnkanal.avro.ReceivedMessage
import no.nav.styringsinformasjon.ApplicationState
import no.nav.styringsinformasjon.Environment
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AltinnKanal2Listener(val env: Environment) : KafkaListener {
    private val log: Logger = LoggerFactory.getLogger(AltinnKanal2Listener::class.java)
    private val kafkaListener: KafkaConsumer<String, ReceivedMessage>

    init {
        val kafkaConfig = kafkaConsumerProperties(env)
        kafkaListener = KafkaConsumer(kafkaConfig)
        kafkaListener.subscribe(listOf(topicAltinnMaalekortMottatt))
    }

    override suspend fun listen(applicationState: ApplicationState) {
        log.info("Started listening to topic $topicAltinnMaalekortMottatt")
        while (applicationState.running) {
            kafkaListener.poll(zeroMillis).forEach {
                val receivedMessage = it.value()
                val archiveReference = receivedMessage.getArchiveReference()
                log.info("Received record from topic $topicAltinnMaalekortMottatt with AR $archiveReference")
                kafkaListener.commitSync()
            }
        }
    }
}
