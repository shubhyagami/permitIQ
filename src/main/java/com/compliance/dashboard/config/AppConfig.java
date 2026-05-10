package com.compliance.dashboard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(ApplicationProperties.class)
public class AppConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    OpenAPI openAPI() {
        final String bearerScheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Permit Monitoring System API")
                        .version("1.0.0")
                        .description("Permit upload, AI extraction, countdown, and IoT APIs"))
                .addSecurityItem(new SecurityRequirement().addList(bearerScheme))
                .schemaRequirement(bearerScheme, new SecurityScheme()
                        .name(bearerScheme)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
