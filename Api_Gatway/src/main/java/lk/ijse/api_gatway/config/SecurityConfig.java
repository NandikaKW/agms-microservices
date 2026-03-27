package lk.ijse.api_gatway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/*
    This class is used to configure security for the API Gateway.
    Here I allow all requests because authentication is handled
    separately using a custom filter.
*/
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http
                // Disable CSRF since this is mainly for API usage
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Allow all requests (custom JWT filter will handle security)
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                );

        return http.build();
    }
}

