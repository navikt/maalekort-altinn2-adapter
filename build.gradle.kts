import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.maalekort"
version = "1.0"

val kluentVersion = "1.68"
val ktorVersion = "1.6.8"
val prometheusVersion = "0.15.0"
val micrometerVersion = "1.8.4"
val spekVersion = "2.0.18"
val mockkVersion = "1.12.3"
val slf4jVersion = "1.7.36"
val logbackVersion = "1.2.11"
val javaxVersion = "2.1.1"
val logstashEncoderVersion = "7.0.1"
val postgresVersion = "42.3.3"
val hikariVersion = "5.0.1"
val flywayVersion = "7.5.2"
val vaultJdbcVersion = "1.3.9"
val jacksonVersion = "2.13.2"
val jacksonDatabindVersion = "2.13.2.2"
val postgresEmbeddedVersion = "0.13.3"
val kafkaVersion = "2.7.0"
val avroVersion = "1.11.0"
val confluentVersion = "6.2.1"
val brukernotifikasjonerSchemaVersion = "2.5.1"

val githubUser: String by project
val githubPassword: String by project

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.5.31"
    id("com.diffplug.gradle.spotless") version "3.18.0"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

allOpen {
    annotation("no.nav.maalekort.annotation.Mockable")
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://packages.confluent.io/maven/")
    maven(url = "https://repo.adeo.no/repository/maven-releases/")
    maven(url = "https://github.com/navikt/vault-jdbc")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.scala-lang" && requested.name == "scala-library" && (requested.version == "2.13.3")) {
            useVersion("2.13.9")
            because("fixes critical bug CVE-2022-36944 in 2.13.6")
        }
    }
}

dependencies {

    // Ktor server
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")

    // API
    implementation("javax.ws.rs:javax.ws.rs-api:$javaxVersion")

    // Logging
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")


    // JSON parsing
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    // Kafka
    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")
    implementation("org.apache.kafka:kafka_2.13:$kafkaVersion") {
        exclude(group = "log4j")
    }
    implementation("io.confluent:kafka-avro-serializer:$confluentVersion")
    implementation("org.apache.avro:avro:$avroVersion")
    implementation("com.github.navikt:brukernotifikasjon-schemas:$brukernotifikasjonerSchemaVersion")
}

configurations.implementation {
    exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-scala_2.13")
}

tasks {
    create("printVersion") {
        println(project.version)
    }

    withType<ShadowJar> {
        manifest.attributes["Main-Class"] = "no.nav.maalekort.BootstrapApplicationKt"
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "14"
    }

    withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
        }
        testLogging.showStandardStreams = true
    }

}
