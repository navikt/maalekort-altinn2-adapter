package no.nav.styringsinformasjon.service

import no.nav.altinnkanal.avro.ReceivedMessage
import no.nav.styringsinformasjon.altinnkanal2.consumer.kafka.ReceivedMessageProcessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MaalekortService : ReceivedMessageProcessor {
    private val log: Logger = LoggerFactory.getLogger(MaalekortService::class.java)
    override suspend fun processMessage(message: ReceivedMessage) {
        log.info("[MAALEKORT SERVICE]: Received ${message.getArchiveReference()}")
        val xml = message.getXmlMessage()
    }
}
