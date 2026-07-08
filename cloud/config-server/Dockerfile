FROM gradle:8.9-jdk21 AS builder
WORKDIR /app

COPY build.gradle ./
COPY settings.gradle ./
COPY gradle.properties ./
COPY gradle ./gradle
COPY gradlew ./
COPY ./cloud/config-server/build.gradle ./cloud/config-server/
COPY ./cloud/config-server/src ./cloud/config-server/src

RUN chmod +x gradlew
RUN ./gradlew :cloud:config-server:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/cloud/config-server/build/libs/*.jar app.jar

EXPOSE 8888
ENTRYPOINT ["java","-jar","app.jar"]
