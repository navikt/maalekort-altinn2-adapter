FROM navikt/java:17
LABEL org.opencontainers.image.source=https://github.com/navikt/maalekort-altinn2-adapter
COPY build/libs/*.jar app.jar