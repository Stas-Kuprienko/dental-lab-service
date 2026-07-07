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

COPY ./ui-mvc-app/build.gradle ./ui-mvc-app/
COPY ./ui-mvc-app/src ./ui-mvc-app/src


RUN chmod +x gradlew
RUN ./gradlew :ui-mvc-app:bootJar --no-daemon


FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/ui-mvc-app/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
