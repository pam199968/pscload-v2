FROM maven:3-jdk-11 AS build
COPY settings-docker.xml /usr/share/maven/ref/
COPY pom.xml /usr/src/app
COPY psc-api/src /usr/src/app/psc-api/src
COPY psc-api/pom.xml /usr/src/app/psc-api
COPY pscload/src /usr/src/app/pscload/src
COPY pscload/pom.xml /usr/src/app/pslcoad
RUN mvn -f /usr/src/app/pom.xml -gs /usr/share/maven/ref/settings-docker.xml clean package

FROM openjdk:11-slim-buster
COPY --from=build /usr/src/app/psc-api/target/psc-api-*.jar /usr/app/psc-api.jar
COPY --from=build /usr/src/app/pscload/target/pscload-*.jar /usr/app/pscload.jar
USER daemon
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/app/pscload.jar"]
