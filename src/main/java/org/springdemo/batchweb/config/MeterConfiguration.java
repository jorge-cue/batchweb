package org.springdemo.batchweb.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeterConfiguration {

    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
