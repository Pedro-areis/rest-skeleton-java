package dev.pedroreis.rest_skeleton_java.config;

import dev.pedroreis.rest_skeleton_java.user.domain.UserService;
import dev.pedroreis.rest_skeleton_java.user.ports.outbound.UserRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserBeanConfig {
    @Bean
    public UserService userService(UserRepositoryPort userRepositoryPort) {
        return new UserService(userRepositoryPort);
    }
}
