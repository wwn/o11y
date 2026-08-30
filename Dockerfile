# syntax=docker/dockerfile:1

FROM eclipse-temurin:26.0.2_10-jdk AS build

ARG MODULE
WORKDIR /workspace

COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon :${MODULE}:quarkusBuild

FROM eclipse-temurin:26.0.2_10-jre

ARG MODULE
WORKDIR /deployments

COPY --from=build /workspace/${MODULE}/build/quarkus-app/ ./

EXPOSE 49252 49253 49254 49255
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
