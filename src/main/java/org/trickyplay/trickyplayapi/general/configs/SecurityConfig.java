package org.trickyplay.trickyplayapi.general.configs;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.trickyplay.trickyplayapi.general.filters.JwtAuthenticationFilter;
import org.trickyplay.trickyplayapi.general.handlers.UnauthorizedHandler;
import org.trickyplay.trickyplayapi.users.enums.Permission;
import org.trickyplay.trickyplayapi.users.enums.Role;
import org.trickyplay.trickyplayapi.users.repositories.TPUserRepository;
import org.trickyplay.trickyplayapi.users.services.TPUserDetailsService;

// @EnableWebSecurity is a marker annotation. It allows Spring to find and automatically apply the class to the global WebSecurity. It's to switch off the default web application security configuration and add your own. EnableWebSecurity will provide configuration via HttpSecurity. It allows to configure access based on urls patterns, the authentication endpoints, handlers etc.
//
// @EnableGlobalMethodSecurity can be added to any class with the @Configuration annotation. @EnableGlobalMethodSecurity is a global configuration annotation that enables method security across the entire application using an annotation-driven approach. You have to explicitly enable the method-level security annotations, otherwise, they’re ignored. EnableGlobalMethodSecurity provides AOP security on methods. Some of the annotations that it provides are PreAuthorize, PostAuthorize. You can also enable @Secured, an older Spring Security annotation, and JSR-250 annotations.
//
// Annotation @EnableGlobalMethodSecurity has become deprecated and was replaced with @EnableMethodSecurity. The rationale behind this change is that with @EnableMethodSecurity property prePostEnabled needed to enable use of @PreAuthorize/@PostAuthorize and @PreFilter/@PostFilter is by default set to true. So you no longer need to write prePostEnabled = true, just annotating your configuration class with @EnableMethodSecurity would be enough.
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(
        prePostEnabled = true, // The prePostEnabled property enables Spring Security pre/post annotations. Supports Spring Expression Language like @PreAuthorize("hasRole('ROLE_ADMIN') or hasAuthority('Admin') and #username == authentication.principal.username)")
        securedEnabled = true, // The securedEnabled property determines if the @Secured annotation should be enabled. Thanks to this flag it becomes possible to use annotations like this @Secured({ "ROLE_USER", "ROLE_ADMIN" })
        jsr250Enabled = true) // The jsr250Enabled property allows us to use the @RoleAllowed annotation. The @RolesAllowed annotation is the JSR-250’s equivalent annotation of the @Secured annotation- @RolesAllowed({ "ROLE_USER", "ROLE_ADMIN" })
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UnauthorizedHandler unauthorizedHandler; // JwtAuthEntryPoint
    private final TPUserRepository tPUserRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new TPUserDetailsService(tPUserRepository);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

//    // The object mapper becomes a bean once you add the spring web starter, let's customize it
//    @Bean
//    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
//        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
//        builder.modules(new JavaTimeModule());
//
//        // for example: Use created_at instead of createdAt
//        builder.propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
//
//        // skip null fields'
//        builder.serializationInclusion(JsonInclude.Include.NON_NULL);
//        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        return builder;
//    }

    @Bean
    public SecurityFilterChain configureApplicationSecurity(HttpSecurity http) throws Exception {
        // ref: https://github.com/spring-projects/spring-security/issues/11939
        // ref: https://stackoverflow.com/questions/74683225/updating-to-spring-security-6-0-replacing-removed-and-deprecated-functionality
        // ref: https://stackoverflow.com/questions/28907030/spring-security-authorize-request-for-certain-url-http-method-using-httpsecu/74633151#74633151
        // Spring Security 6.0 - replacing Removed and Deprecated functionality for securing requests
        // In Spring Security 6.0, antMatchers() as well as other configuration methods for securing requests (namely mvcMatchers() and regexMatchers()) have been removed from the API. Several flavors of requestMatchers() method has been provided as a replacement.
        // An overloaded method requestMatchers() was introduced as a uniform mean for securing requests. The flavors of requestMatchers() facilitate all the ways of restricting requests that were supported by the removed methods.
        // Also, method authorizeRequests() has been deprecated and shouldn't be used anymore. A recommended replacement - authorizeHttpRequests()
        //
        // Even if you're using an earlier Spring version in your project and not going to update to Spring 6 very soon, antMatchers() isn't the best tool you can choose for securing requests to your application. While applying security rules using antMatchers() you need to be very careful because if you secure let's say path "/foo" these restrictions wouldn't be applied to other aliases of this path like "/foo/", "/foo.thml". As a consequence, it's very easy to misconfigure security rules and introduce a vulnerability (for instance, a path that is supposed to be accessible only for Admins becomes available for any authenticated user, it's surprising that none of the answers above mentions this).
        //
        // Spring security deprecated way to intercept paths:
        // http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // http.logout().logoutUrl("/logout").addLogoutHandler(logoutHandler).logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
        // http.exceptionHandling()
        //        .authenticationEntryPoint(
        //                (request, response, ex) -> {
        //                    response.sendError(
        //                            HttpServletResponse.SC_UNAUTHORIZED,
        //                            ex.getMessage()
        //                    );
        //                }
        //        );


        // ref: https://stackoverflow.com/questions/28907030/spring-security-authorize-request-for-certain-url-http-method-using-httpsecu/74633151#74633151
        // According to the documentation, the recommended way to restrict access to certain URL since 5.8 is to use HttpSecurity.authorizeHttpRequests(), which as well as its predecessor comes in two flavors:
        // 1. A parameterless version, which returns an object responsible for configuring the requests. So we can chain requestMatchers() calls directly on it.
        // 2. And the second one expecting an instance of the Customizer interface which allow to apply enhanced DSL (domain-specific language), by using lambda expressions. Lambda DSL makes the configuration more intuitive to read by visually grouping configuration options and eliminates the need to use method and().
        // Which version of authorizeHttpRequests() to use is a stylistic choice (both are valid and supported in 6.0).
        //
        // requestMatchers() now has four overloaded versions:
        // 1. requestMatchers( String ... ) - expects a varargs of String patterns. This matcher uses the same rules for matching as Spring MVC. I.e. it would act in the same way as old mvcMatchers(), so that pattern /foo would match all existing aliases of that path like "/foo", "/foo/", "/foo.html". All other versions of requestMatchers() have the same matching behavior, it eliminates the possibility of misconfiguration, which was an Achilles' heel of antMatchers(). Note that the corresponding restriction (hasRole(), access(), etc.) would be applied to any matching request regardless of its HttpMethod.
        // Example:
        // .requestMatchers("/foo/*").hasRole("ADMIN") // only authenticated user with role ADMIN can access path /foo/something
        // .requestMatchers("/bar/*", "/baz/*").hasRole("ADMIN") // only authenticated requests to paths /foo/something and /baz/something  are allowed
        // 2. requestMatchers( HttpMethod ) - expects an HttpMethod as argument. The corresponding restriction (hasRole(), access(), etc.) would be applied to any request handled by the current SecurityFilterChain having specified HttpMethod. If null provided as an argument, any request would match.
        // Example:
        // .requestMatchers(HttpMethod.POST, "/bar/**").hasAnyRole("USER", "ADMIN") // any authenticated POST-requests should from an ADMIN or USER are allowed
        // 3. requestMatchers( HttpMethod, String ... ) - this method combines the previous two, it allows to specify an HttpMethod and one or more String patterns.
        //Example:
        // .requestMatchers(HttpMethod.POST, "/bar/**").hasAnyRole("USER", "ADMIN") // any POST-request should be authenticatd
        // .requestMatchers(HttpMethod.DELETE, "/baz/**").hasRole( "ADMIN") // only ADMINs can issue DELETE-requests to these paths
        // 4. requestMatchers( RequestMatcher ... ) - the last version is probably the most flexible one, it allows to provide an arbitrary number of combines RequestMatcher instances. We don't need to implement this interface ourselves (unless there's special need), there are several implementations available out of the box including RegexRequestMatcher (which can be used to replace outdated regexMatchers()).
        // Example:
        // .requestMatchers(new RegexRequestMatcher("/foo/bar", "POST")).authenticated()

        // ref: https://developer.okta.com/blog/2019/06/20/spring-preauthorize
        // HttpSecurity method rejects the request early, in a web request filter, before controller mapping has occurred. In contrast, the @PreAuthorize assessment happens later, directly before the execution of the controller method. This means that configuration in HttpSecurity is applied before @PreAuthorize. Moreover, HttpSecurity is tied to URL endpoints while @PreAuthorize is tied to controller methods and is actually located within the code adjacent to the controller definitions. Having all of your security in one place and defined by web endpoints has a certain neatness to it, especially in smaller projects, or for more global settings; however, as projects get larger, it may make more sense to keep the authorization policies near the code being protected, which is what the annotation-based method allows.

        http
                .cors(AbstractHttpConfigurer::disable) // .csrf().disable() "disabling SOP"- SOP is the Same Origin Policy – mechanism implemented inside each browser that prevents some requests to be executed when origin of page that makes a request differs from the origin of the requested resource
                .csrf(AbstractHttpConfigurer::disable) // our stateless API uses jwt token-based authentication, we don't need CSRF protection
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // in STATELESS mode Spring Security will never create a HttpSession, and it will never use it to get the SecurityContext.
                .authenticationProvider(authenticationProvider())
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(h -> h.authenticationEntryPoint(unauthorizedHandler)) // Spring security exceptions are commenced at the AuthenticationEntryPoint
                .securityMatcher("/**") // it makes the security configuration to be applied to all controllers, ** is a pattern to match any number of directories and subdirectories in a URL. The * pattern is a pattern that matches any URL and has exactly one level of a subdirectory
                .authorizeHttpRequests(registry -> registry // .authorizeHttpRequests().requestMatchers('').hasAnyRole()
                        // ref: https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html
                        // Ant is the default language that Spring Security uses to match requests.
                        .requestMatchers("/auth/**").permitAll() // Relax security for authentication endpoints
                        .requestMatchers(HttpMethod.GET, "/comments").permitAll()  // Relax security for public resources
                        .requestMatchers(HttpMethod.POST, "/comments").hasAuthority(Permission.USER_CREATE.getPermission())
                        .requestMatchers(HttpMethod.PATCH, "/comments").hasAuthority(Permission.USER_UPDATE.getPermission())
                        .requestMatchers(HttpMethod.DELETE, "/comments").hasAnyRole(Role.USER.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/replies/**").permitAll()  // Relax security for public resources
                        .requestMatchers(HttpMethod.POST, "/replies").hasAuthority(Permission.USER_CREATE.getPermission())
                        .requestMatchers(HttpMethod.PATCH, "/replies").hasAuthority(Permission.USER_UPDATE.getPermission())
                        .requestMatchers(HttpMethod.DELETE, "/replies").hasAnyRole(Role.USER.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/users/**").permitAll()  // Relax security for public resources
                        .requestMatchers(HttpMethod.PATCH, "/account").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.BANNED.name())
                        .requestMatchers(HttpMethod.DELETE, "/account").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.BANNED.name())
                        .requestMatchers(HttpMethod.GET, "/account").hasAnyRole(Role.USER.name(), Role.ADMIN.name(), Role.BANNED.name())
                        .requestMatchers(HttpMethod.PATCH, "/account/{id}/ban-account", "/account/{id}/unban-account").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PATCH, "/account/{id}/grant-admin-permissions").hasRole(Role.ADMIN.name())
                        .anyRequest().authenticated()
                );
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}