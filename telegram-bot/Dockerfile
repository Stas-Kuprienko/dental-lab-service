FROM gradle:8.9-jdk21 AS builder
WORKDIR /app


COPY build.gradle ./
COPY settings.gradle ./
COPY gradle.properties ./
COPY gradle ./gradle
COPY gradlew ./

COPY ./common/models/build.gradle ./common/models/
COPY ./common/models/src ./common/models/src

COPY ./common/dental-client/build.gradle ./common/dental-client/
COPY ./common/dental-client/src ./common/dental-client/src

COPY ./telegram-bot/build.gradle ./telegram-bot/
COPY ./telegram-bot/src ./telegram-bot/src


RUN chmod +x gradlew
RUN ./gradlew :telegram-bot:bootJar --no-daemon


FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/telegram-bot/build/libs/*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java","-jar","app.jar"]
