package dev.pedroreis.rest_skeleton_java.config;

import dev.pedroreis.rest_skeleton_java.auth.domain.AuthService;
import dev.pedroreis.rest_skeleton_java.auth.ports.outbound.TokenPort;
import dev.pedroreis.rest_skeleton_java.user.ports.outbound.PasswordHasherPort;
import dev.pedroreis.rest_skeleton_java.user.ports.outbound.UserRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthBeanConfig {
    @Bean
    public AuthService authService(UserRepositoryPort userRepositoryPort,
                                   PasswordHasherPort passwordHasherPort,
                                   TokenPort tokenPort) {
        return new AuthService(userRepositoryPort, passwordHasherPort, tokenPort);
    }
}
