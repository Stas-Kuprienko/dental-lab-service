FROM gradle:8.9-jdk21 AS builder
WORKDIR /app

COPY build.gradle ./
COPY settings.gradle ./
COPY gradle.properties ./
COPY gradle ./gradle
COPY gradlew ./
COPY ./cloud/eureka-server/build.gradle ./cloud/eureka-server/
COPY ./cloud/eureka-server/src ./cloud/eureka-server/src

RUN chmod +x gradlew
RUN ./gradlew :cloud:eureka-server:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/cloud/eureka-server/build/libs/*.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java","-jar","app.jar"]
