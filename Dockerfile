# syntax=docker/dockerfile:1.7

FROM gradle:8.14.3-jdk21 AS build

ARG MODULE
WORKDIR /workspace

COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon :${MODULE}:quarkusBuild

FROM eclipse-temurin:21-jre-jammy

ARG MODULE
WORKDIR /deployments

COPY --from=build /workspace/${MODULE}/build/quarkus-app/ ./

EXPOSE 49252 49253 49254 49255
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
