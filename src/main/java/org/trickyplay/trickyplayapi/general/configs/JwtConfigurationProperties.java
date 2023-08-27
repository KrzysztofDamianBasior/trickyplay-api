package org.trickyplay.trickyplayapi.general.configs;

// Before Spring 2.2, main class should have @EnableConfigurationProperties(MyProperties .class) or use @Configuration above MyProperties class.
// But after 2.2 Spring finds and registers @ConfigurationProperties classes via classpath scanning so there is no need for these annotations, you can just have @ConfigurationPropertiesScan("property package path") annotation to scan custom locations for configuration property classes.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// @Configuration- decorator not necessary as we decorated the main class with @ConfigurationPropertiesScan
@Validated
@ConfigurationProperties(prefix = "application.security.jwt")
@Data // configuration properties needs getters and setters
public class JwtConfigurationProperties {
    @NotBlank
    private String secretKey;

    @NotBlank
    @Pattern(regexp = "^\\d+$") // digits only
    private String refreshTokenExpiration;

    @NotBlank
    @Pattern(regexp = "^\\d+$") // digits only
    private String accessTokenExpiration;
}
