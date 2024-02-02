package org.trickyplay.trickyplayapi;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;

import org.testcontainers.containers.MySQLContainer;

// Every integration test should extend BaseIntegrationTest class. This way, you centralize the configuration.
// It may be useful to cache the image beforehand using docker pull mysql:8.1 to avoid issues with the download

//@SpringBootTest( // can be used to load the entire Spring context for your tests, might lead to very long-running test suites
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT // the application will be started under a random and available port, so during testing there shouldn't be any conflict
@ActiveProfiles("test")
// @ActiveProfile is used to activate a particular profile(s) during junit test class execution to load the required configuration or Component.
public abstract class BaseIntegrationTest {
    // The spring-boot-testcontainers library supplies the new @ServiceConnection annotation. It indicates that a field or method represents a container providing a service Spring can connect to. The connection will be configured automatically, so you don’t need a @DynamicPropertySource method.
    @ServiceConnection
    public static final MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.1")
            .withDatabaseName("testcontainer")     // By default, MySQLContainer creates a database called "test" accessible with the credentials "test\test"
            .withUsername("test")
            .withPassword("test");

    // Within the static block we are setting the reuse parameter, so that the container will continue running until we manually stop it. Usually that's not causing issues and makes testing the application faster - the Docker container can be stopped manually if required.
    static {
        mySQLContainer.withUrlParam("serverTimezone", "UTC") //  Ideally this should exactly match the version that is also used in production
                .withReuse(true)
                .start();
    }

    // ref: https://danielme.com/2023/04/13/testing-spring-boot-docker-with-testcontainers-and-junit-5-mysql-and-other-images/
    // Spring Boot requires the connection parameters for the MySQL server running inside the container to create the DataSource bean for the tests. The testing setup must replace the values you already have in the application.properties file corresponding to the database used by the project. Testcontainers exposes the ports published by the container on host random free ports. The goal is to ensure that running services don’t already use the chosen ports—two processes can’t listen on the same port. You have to figure out the port number and find a way to declare it in the Spring configuration. Since this number is variable, it isn’t feasible to define it in a configuration file or in the @SpringBootTest annotation by using the properties attribute. You solve the first challenge by asking mySQLContainer for the port through which Testcontainers exposes the standard MySQL port:
    // int port = mySQLContainer.getMappedPort(3306); // getMappedPort is a method provided by GenericContainer
    // String jdbcUrl = mySQLContainer.getJdbcUrl(); // method that returns the complete URL
    // Originally a special method annotated with @DynamicPropertySource was required to provide the connection credentials to the application context. With Spring Boot 3.1.0 that's not required anymore for most databases and simply adding @ServiceConnection will do exactly that.
         /*
         // As of Spring Boot 3.1, with @ServiceConnection you don't need the following method to set up the connection
         @DynamicPropertySource
         private static void setupProperties(DynamicPropertyRegistry registry) {
            registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
            registry.add("spring.datasource.username", mySQLContainer::getUsername);
            registry.add("spring.datasource.password", mySQLContainer::getPassword);
        }*/
    // Fortunately, since Spring Boot 3.1 there’s a more straightforward solution. The spring-boot-testcontainers library supplies the new @ServiceConnection annotation. It indicates that a field or method represents a container providing a service Spring can connect to. The connection will be configured automatically, so you don’t need a @DynamicPropertySource method. However, your container must be represented by a class supported by @ServiceConnection.
}
