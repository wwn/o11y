FROM eclipse-temurin:26.0.2_10-jdk AS build
COPY . /code
WORKDIR /code
RUN ./gradlew :o11y-app:build

FROM eclipse-temurin:26.0.2_10-jre
ENV LANGUAGE='en_US:en'
COPY --from=build /code/o11y-app/build/quarkus-app/lib/ /deployments/lib/
COPY --from=build /code/o11y-app/build/quarkus-app/*.jar /deployments/
COPY --from=build /code/o11y-app/build/quarkus-app/app/ /deployments/app/
COPY --from=build /code/o11y-app/build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 49255
USER 185
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar $JAVA_APP_JAR" ]
