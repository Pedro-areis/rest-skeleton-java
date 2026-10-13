package dev.pedroreis.rest_skeleton_java.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {
    /** Relógio do sistema. Os testes passam um {@code Clock} fixo direto no construtor. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
