FROM eclipse-temurin:17 AS build
RUN echo 'First stage...';

WORKDIR /usr/app/

COPY build.gradle settings.gradle gradlew /usr/app/
COPY gradle /usr/app/gradle
COPY src/main /usr/app/src/main

RUN ./gradlew clean build

FROM eclipse-temurin:17
RUN echo 'Second stage...';

COPY --from=build /usr/app/build/libs/*.jar ./app.jar

ENV ACTIVE_PROFILE = prod

ARG DESCRIPTION="TrickyPlay API is a web service designed to manage users, comments and replies. Designed with exploreability in mind, the REST-compatible API uses unique resource addresses and HATEOAS hypermedia methods that provide the client with information about potential actions that can be performed."
ARG BUILD_TAG=local
ARG VERSION=local

LABEL maintainer="krzysztofbasior"
LABEL description=${DESCRIPTION}
LABEL version=${VERSION}
LABEL build_tag=${BUILD_TAG}

#COPY HealthCheck.java .
#HEALTHCHECK --interval=5s --timeout=3s --retries=2 CMD ["java", "-Durl=http://localhost:80/actuator/health" , "./HealthCheck.java", "||", "exit", "1"]

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Dspring.profiles.active=${ACTIVE_PROFILE} -jar /app.jar ${0} ${@}"]
