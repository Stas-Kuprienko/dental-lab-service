FROM gradle:8.9-jdk21 AS builder
WORKDIR /app

COPY build.gradle ./
COPY settings.gradle ./
COPY gradle.properties ./
COPY gradle ./gradle
COPY gradlew ./
COPY ./common/models/build.gradle ./common/models/
COPY ./common/models/src ./common/models/src
COPY ./dental-service/build.gradle ./dental-service/
COPY ./dental-service/src ./dental-service/src

RUN chmod +x gradlew
RUN ./gradlew :dental-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/dental-service/build/libs/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]
