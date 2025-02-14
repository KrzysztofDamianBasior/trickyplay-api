package org.trickyplay.trickyplayapi.general.configs;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManagementConfig {
    //actuator/metrics endpoint exposes a wealth of information, if you want to view your custom counter, you can access it via:
    //    GET/actuator/metrics/my.counter
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> usernameTakenRegistry() {
        return registry -> registry.config().namingConvention().name("controllers.username-taken", Meter.Type.COUNTER);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> tokenRefreshedRegistry() {
        return registry -> registry.config().namingConvention().name("controllers.token-refreshed", Meter.Type.COUNTER);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> deleteAccountRegistry() {
        return registry -> registry.config().namingConvention().name("controllers.account-deleted", Meter.Type.COUNTER);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> signedUpRegistry() {
        return registry -> registry.config().namingConvention().name("controllers.signed-up", Meter.Type.COUNTER);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> signedInRegistry() {
        return registry -> registry.config().namingConvention().name("controllers.signed-in", Meter.Type.COUNTER);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> signedOutRegistry() {
        return registry -> registry.config().namingConvention().name("controllers.signed-out", Meter.Type.COUNTER);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> commentLengthRegistry() {
        return registry -> registry.config().namingConvention().name("controllers.added-comment-length", Meter.Type.DISTRIBUTION_SUMMARY);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> replyLengthRegistry() {
        return registry -> registry.config().namingConvention().name("controllers.added-reply-length", Meter.Type.DISTRIBUTION_SUMMARY);
    }
}
