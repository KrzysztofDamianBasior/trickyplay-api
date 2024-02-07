# Read Me First

TrickyPlay API is a web service designed to manage users, comments and replies. Designed with exploreability in mind, the REST-compatible API uses unique resource addresses and HATEOAS hypermedia methods that provide the client with information about potential actions that can be performed. 

### Features
* API compatible with the REST architecture, uses unique resource addresses and HATEOAS hypermedia methods compatible with IANA link relations
* JWT authentication with access and refresh tokens.
* User can generate account activity history in pdf format
* User can be logged in on many devices. The user can decide which device he or she wants to log out of or can log out of all devices at once.
* Role-based authorization (rbac- role based access control) with Spring Security
* 3 profiles - test, development and production
* Customized log functionality using logback
* Database schema management using liquibase
* Password encryption using BCrypt
* Used JUnit as the testing framework, Mockito for mocking, AsseretJ for creating assertions and Testcontainers for integration tests

### Technologies
Let me explain why each dependency is used:
* spring-boot-starter-security: starter that bundles all the Spring Security-related dependencies together. Spring Security is a highly customizable framework that provides authentication, authorization, and protection against common attacks. It is the preferred choice for implementing application-level security in Spring applications
* spring-boot-starter-web: starter module in Spring Boot that provides a quick and easy way to build web applications. It includes all the necessary dependencies for building a web application, such as Tomcat, Spring MVC, and Jackson
* spring-boot-starter-data-jpa: starter module that provides a quick and easy way to connect your Spring application with a relational database efficiently. It includes all the necessary dependencies for building a data access layer using JPA and Hibernate ORM, which provides a powerful repository abstraction that reduces the amount of boilerplate code required to implement data access layers.
* spring-boot-starter-validation: starter module that provides a quick and easy way to validate user input. It includes all the necessary dependencies for building a validation layer using the Bean Validation framework and Hibernate Validator. By using spring-boot-starter-validation, you can quickly create a validation layer that validates user input. If the input fails validation, Spring Boot automatically returns an HTTP 400 Bad Request response to the client
* spring-boot-starter-hateoas: starter module that provides a quick and easy way to create REST representations that follow the principle of HATEOAS (Hypertext as the Engine of Application State). By using spring-boot-starter-hateoas, you can create a resource representation that guides the client through the application by returning relevant information about the next potential steps, along with each response. The module provides auto-configuration for creating metadata and associating it with the resource representation.
* spring-boot-starter-test: starter module that provides a quick and easy way to write tests for your Spring Boot application. It includes all the necessary dependencies for building unit and integration tests, such as JUnit, Spring Test, and AssertJ. By using spring-boot-starter-test, you can quickly create unit tests that can run in isolation as well as integration tests that will bootstrap Spring context before executing tests.
* spring-security-test: module that provides support for testing Spring Security applications. You can use @WithMockUser annotation to test your application with a mock user. You can also use @WithAnonymousUser annotation to test your application with an anonymous user.
* spring-boot-devtools: module that provides a set of tools to enhance the development experience. Spring-boot-devtools provides features such as automatic restarts and live reloads. Whenever files change in the classpath, applications using spring-boot-devtools will cause the application to restart. The benefit of this feature is the time required to verify the changes made is considerably reduced.
* mysql-connector-java: JDBC driver for MySQL that allows Java programs to connect to a MySQL database. It is the official JDBC driver for MySQL and is developed by Oracle. The driver provides support for all the standard JDBC features, including transactions, prepared statements, and stored procedures.
* jjwt: is a Java library that provides JSON Web Token creation and verification. JJWT is a pure Java implementation based exclusively on the JOSE Working Group RFC specifications. It supports all the standard JWT features, including signing, encryption, and claims.
* liquibase-core: module that provides the core functionality for managing database changes. By using liquibase-core, you can quickly create a changelog file that describes changes to your database schema over time. The module provides support for all the standard Liquibase features, including rollback, diff, and updateSql.
* lombok: provides a set of user-friendly annotations that generate the code at compile time, helping the developers save time and space and improving code readability. With Lombok, you can minimize or remove the boilerplate code, such as getters, setters, constructors, and logging variables.
* testcontainers: it is a Java library that allows you to run Docker containers as part of your integration tests. It provides a simple API to start and stop these containers. By using Testcontainers, you can test your application against a real database instance, message broker, or any other service that your application depends on. This way, you can ensure that your application works correctly in a production-like environment. For instance, if you want to test your Spring Boot application with a MySQL database, you can use Testcontainers to start a MySQL container and run your tests against it. This way, you can test your application’s behavior with a real MySQL instance, instead of an embedded database like H2.
* openpdf: OpenPDF is a free and open-source Java library that can be used to create, edit, and read PDF files. It provides a simple API for generating PDF documents from scratch or modifying existing ones. OpenPDF can be helpful for creating reports with Java Spring Boot as it allows you to generate PDF reports dynamically and efficiently. You can use OpenPDF to read data from a database or other sources, format it, and then generate a PDF report that can be downloaded or sent to a printer. To get started with OpenPDF in Java Spring Boot, you can add the OpenPDF dependency to your project’s pom.xml file. Once you have added the dependency, you can use the OpenPDF API to create a new PDF document, add content to it, and then save it to a file or stream.

### Getting Started
To get started with this project, you will need to have the following installed on your local machine:
* JDK 17+
* Docker desktop (it may be useful to cache the database image beforehand using- docker pull mysql:8.1, to avoid issues with the download)

To build and run the project, follow these steps:
* Clone the repository: `git clone https://github.com/ali-bouali/spring-boot-3-jwt-security.git`
* Navigate to the project directory
* Add database "trickyplay" to MySQL
* Set environment variables:
  * DEFAULT_ACCESS_TOKEN_EXPIRATION={time to expire jwt in milliseconds for the default profile}
  * PROD_ACCESS_TOKEN_EXPIRATION={time to expire jwt in milliseconds for the production profile}
  * TEST_ACCESS_TOKEN_EXPIRATION={time to expire jwt in milliseconds for the test profile}
  * DEFAULT_REFRESH_TOKEN_EXPIRATION={time until the refresh token expires in milliseconds for the default profile}
  * PROD_REFRESH_TOKEN_EXPIRATION={time until the refresh token expires in milliseconds for the default profile}
  * TEST_REFRESH_TOKEN_EXPIRATION={time until the refresh token expires in milliseconds for the default profile}
  * DEFAULT_APPLICATION_PORT={port on which the application listens for the default profile}
  * PROD_APPLICATION_PORT={port on which the application listens for the production profile}
  * DEFAULT_JWT_SECRET_KEY={jwt secret key for the default profile}
  * PROD_JWT_SECRET_KEY={jwt secret key for the production profile}
  * TEST_JWT_SECRET_KEY={jwt secret key for the test profile}
  * DEFAULT_MYSQL_DATABASE={database name for the default profile}
  * PROD_MYSQL_DATABASE={database name for the production profile}
  * DEFAULT_MYSQL_HOST={database host for the default profile}
  * PROD_MYSQL_HOST={database host for the production profile}
  * DEFAULT_MYSQL_PASSWORD={database password for the default profile}
  * PROD_MYSQL_PASSWORD={database password for the production profile}
  * DEFAULT_MYSQL_PORT={database port for the default profile}
  * PROD_MYSQL_PORT={database port for the production profile}
  * DEFAULT_MYSQL_USERNAME={database username for the default profile}
  * PROD_MYSQL_USERNAME={database username for the production profile}
  * LOG_FILE={relative path to log file like logs/app-logback.log}
* To build and run the application use: `./gradlew bootRun`
* The application will be available at http://localhost:${DEFAULT_APPLICATION_PORT}.

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.1.2/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.1.2/gradle-plugin/reference/html/#build-image)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#using.devtools)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#appendix.configuration-metadata.annotation-processor)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#web)
* [Spring HATEOAS](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#web.spring-hateoas)
* [Spring Security](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#web.security)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#data.sql.jpa-and-spring-data)
* [Liquibase Migration](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#howto.data-initialization.migration-tool.liquibase)
* [Validation](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#io.validation)
* [Testcontainers](https://java.testcontainers.org/)
* [JJWT](https://github.com/jwtk/jjwt)
* [OpenPdf](https://github.com/LibrePDF/OpenPDF?tab=readme-ov-file)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Building a Hypermedia-Driven RESTful Web Service](https://spring.io/guides/gs/rest-hateoas/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)
* [Testcontainers](https://java.testcontainers.org/)
* [JJWT](https://github.com/jwtk/jjwt#install)
* [OpenPdf](https://github.com/LibrePDF/OpenPDF?tab=readme-ov-file)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
