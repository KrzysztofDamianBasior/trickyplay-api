FROM eclipse-temurin:17 AS build
RUN echo 'First stage...';

WORKDIR /usr/app/
# CMD ["sh", "-c", "echo ${HOME}"]

COPY build.gradle settings.gradle gradlew /usr/app/
COPY gradle /usr/app/gradle
COPY src/main /usr/app/src/main

RUN ./gradlew clean build

FROM eclipse-temurin:17
RUN echo 'Second stage...';

COPY --from=build /usr/app/build/libs/*.jar ./app.jar

ENV ACTIVE_PROFILE=prod

ARG DESCRIPTION="TrickyPlay API is a web service designed to manage users, comments and replies. Designed with exploreability in mind, the REST-compatible API uses unique resource addresses and HATEOAS hypermedia methods that provide the client with information about potential actions that can be performed."
ARG BUILD_TAG=local
ARG VERSION=local

LABEL maintainer="krzysztofbasior"
LABEL description=${DESCRIPTION}
LABEL version=${VERSION}
LABEL build_tag=${BUILD_TAG}

COPY HealthCheck.java .

RUN if ["$ACTIVE_PROFILE" = "prod"]; \
    then export APPLICATION_PORT="$DEFAULT_APPLICATION_PORT"; \
    elif ["$ACTIVE_PROFILE" = "test"]; \
    then export APPLICATION_PORT="$TEST_APPLICATION_PORT"; \
    else export APPLICATION_PORT="$DEFAULT_APPLICATION_PORT"; \
    fi
#Alternative solution to set APPLICATION_PORT ->
#FROM eclipse-temurin:17 AS base
#
#FROM base AS branch-version-prod
#ENV VAR=TRUE
#
#FROM base AS branch-version-test
#ENV VAR=FALSE
#
#FROM branch-version-${ACTIVE_PROFILE} AS final
#RUN echo "VAR is equal to ${VAR}"

#RUN keytool -import -noprompt -trustcacerts -alias myFancyAlias -file /path/to/my/cert/myCert.cer -keystore /app/config/keystore.jks -storepass changeit
# we can bypass command prompts for a password and a confirmation by adding the storepass and noprompt arguments, this comes especially handy when running keytool from a script

# HEALTHCHECK --interval=5s --timeout=3s --retries=4 CMD ["sh", "-c", "java -Durl=https://${HEALTHCHECK_HOST}/actuator/health ./HealthCheck.java || exit 1"]

# ----------------------------------------------------------------------------------------------------
# What to remember when setting the JAVA_OPTS variable?
# 1. JAVA_OPTS (-e JAVA_OPTS="-Xmx512m -Xms256m") is used to configure JVM-related options, CATALINA_OPTS (-e CATALINA_OPTS="-Dcatalina.http.port=8082") configure Tomcat server-related configurations.
# 2. The -server option is useful to let the JVM knows that the app is to be excuted in a server environnement, which will leads to some changement including GC dedicated algorithm for server environnements and some other behaviors which may be found in the offical documentation.
# 3. To stop stacktraces truncating in logs increase -XX:MaxJavaStackTraceDepth JVM option like: java -XX:MaxJavaStackTraceDepth=1000000
# 4. The JVM takes as much memory as you give it, and it will perform a process called Garbage collection to free up space once it decides it should do so. In Docker environments with a hard limit on the available amount of memory, the JVM will crash when it tries to allocate more memory than is available. If you don't tell your JVM how much memory it can use, it will use the system defaults, which depend on your systems memory and the amount of cores you have. However, that doesn't mean that your application needs it. You shouldn't have to worry about the stack leaking memory (it is highly uncommon). The only time you can have the stack get out of control is with infinite (or really deep) recursion. You should care about total memory and working memory. Working memory is the memory that is left over after a garbage collection -objects on heap that cannot be destroyed because they're still in use. To limit the total memory used by process (the total memory used by the JVM not just the heap) use the arguments -Xms<memory> -Xmx<memory>. Use M or G after the numbers for indicating Megs and Gigs of bytes respectively. -Xms indicates the minimum and -Xmx the maximum. For Example: -Xmx1024m (that will allow a max of 1GB of memory for the JVM). Here are some other parameters that can be tuned, worth the effort to investigate per application and specify:
#    -Xss ~you can set to 256kb. Unless your application has really deep stacks (recursion), going from 1 MB to 256kb per thread saves a lot of memory. If your app is more heavy use Xss512k -this will limit each threads stack memory to 512KB instead of the default 1MB
#    -XX:ReservedCodeCacheSize ~good value is 64MB. Peak "CodeCache" usage is often during application startup, going from 192 MB to 64 MB saves a lot of memory which can be used as heap. Applications that have a lot of active code during runtime (e.g. a web-application with a lot of endpoints) may need more "CodeCache". If "CodeCache" is too low, your application will use a lot of CPU without doing much (this can also manifest during startup: if "CodeCache" is too low, your application can take a very long time to startup). "CodeCache" is reported by the JVM as a non-heap memory region, it should not be hard to measure.
#    -XX:+UseSerialGC ~this will perform garbage collection inline with the thread allocating the heap memory instead of a dedicated GC thread(s)
#    -XX:MaxRAM=72m ~this will restrict the JVM's calculations for the heap and non heap managed memory to be within the limits of this value.
#    server.tomcat.max-threads = 1 ~this following property inside your application.properties file will limit the number of HTTP request handler threads to 1 (default is 200)
#    -Xms50M ~xms initial memory
#    -Xmx50M ~maximum size of the memory allocation pool, which includes the heap, the garbage collector’s survivor space, and other pools so there is a difference between the -Xmx parameter and the Max memory reported by the JVM
# Example:
#    -Xmx64M -Xms64M -XX:MaxPermSize=64M -Xss256k -XX:ReservedCodeCacheSize=64M -XX:MaxDirectMemorySize=10M -XX:MaxMetaspaceSize=121289K -Xmx290768K
# To configure your -Xmx you'll have to see how your application behaves after trying it out:
#•	Configure it below your normal memory usage and your application will go out of memory, throwing an OutOfMemoryError.
#•	Configure it too low but above your minimal memory usage, and you will see a huge performance hit, due to the garbage collector continuously having to free memory.
#•	Configure it too high and you'll reserve memory you won't need in most of the cases, so wasting too much resources.
#Probably defaults has a way too high -Xmx value. Jvm heap setting pattern doesn't impose restrictions on the values used however it is prefered to work in powers of 2. Go with at least working memory, increase it a bit, see how it performs, and adjust if it goes out of memory or performs worse. For a minimum total memory you can assume 400 MB for a small Spring Boot application, a good starting point from which you can start adjusting it is 512 MB.
ENTRYPOINT ["sh", "-c", "java -server ${JAVA_OPTS} ${CATALINA_OPTS} -Dspring.profiles.active=${ACTIVE_PROFILE} -jar /app.jar ${0} ${@}"]

#You can use actuator's /health endpoint to see the heap memory usage or measure memory by using a monitoring tool like jvisualvm.
#You can check the RAM used by app running in a container by running:
#    docker stats containername
#To check CPU and memory configuration:
#    docker info | grep -iE "CPUs|Memory"
#You can verify how is the default Java heap size determined using:
#    java -XX:+PrintFlagsFinal -version | grep HeapSize
#You will get initial heap memory (like 256MiB) and a maximum heap size of (like 4GiB). The bare minimum you'll get away with is around 72M total memory on the simplest of Spring Boot applications with a single controller and embedded Tomcat. Throw in Spring Data REST, Spring Security and a few JPA entities and you'll be looking at 200M-300M minimum.
#To look at that heap size run:
#    $ java -XX:+PrintFlagsFinal -version | grep -Ei "maxheapsize|maxram"
#This outputs MaxHeapSize, MaxRAM, MaxRAMFraction, MaxRAMPercentage, SoftMaxHeapSize