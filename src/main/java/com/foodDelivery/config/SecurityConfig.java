
package com.foodDelivery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from the React/Vite frontend.
        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept")
        );

        // Needed if your frontend later uses cookies or credentials.
        // Do not enable this unless required by your authentication design.
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())

                // Keep CSRF protection enabled.
                .authorizeHttpRequests(auth -> auth

                        // Anyone can register a customer.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/register"
                        ).permitAll()

                        // Public restaurant browsing.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/restaurants",
                                "/api/restaurants/**"
                        ).permitAll()

                        // Only owners and admins can modify restaurants.
                        .requestMatchers(
                                "/api/restaurants/**"
                        ).hasAnyRole(
                                "RESTAURANT_OWNER",
                                "ADMIN"
                        )

                        // Only admins can access user-directory endpoints.
                        .requestMatchers(
                                "/api/users/**"
                        ).hasRole("ADMIN")

                        // Authenticated roles for order endpoints.
                        .requestMatchers(
                                "/api/orders/**",
                                "/api/order-items/**"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "DELIVERY_PARTNER",
                                "ADMIN"
                        )

                        // Only admins can create notifications.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/notifications"
                        ).hasRole("ADMIN")

                        // Everything else requires authentication.
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
