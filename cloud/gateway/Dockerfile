FROM gradle:8.9-jdk21 AS builder
WORKDIR /app

COPY build.gradle ./
COPY settings.gradle ./
COPY gradle.properties ./
COPY gradle ./gradle
COPY gradlew ./
COPY ./cloud/gateway/build.gradle ./cloud/gateway/
COPY ./cloud/gateway/src ./cloud/gateway/src

RUN chmod +x gradlew
RUN ./gradlew :cloud:gateway:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/cloud/gateway/build/libs/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]
