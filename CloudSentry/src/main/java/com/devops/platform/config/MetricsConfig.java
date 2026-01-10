package com.devops.platform.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Micrometer/Prometheus metrics configuration.
 */
@Configuration
public class MetricsConfig {

    /**
     * Customizes the meter registry with common tags.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(List.of(
                        Tag.of("application", "devops-platform"),
                        Tag.of("service", "cloudsentry")));
    }

    /**
     * Enables @Timed annotation support.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
