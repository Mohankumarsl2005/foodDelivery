
package com.foodDelivery.restaurant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Anyone can register a new customer.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/register"
                        ).permitAll()

                        // Restaurant browsing is public.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/restaurants",
                                "/api/restaurants/**"
                        ).permitAll()

                        // Only restaurant owners and admins can modify restaurants.
                        .requestMatchers("/api/restaurants/**")
                        .hasAnyRole("RESTAURANT_OWNER", "ADMIN")

                        // Only admins can access user-directory endpoints.
                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")

                        // Customers, delivery partners, and admins can access orders.
                        .requestMatchers(
                                "/api/orders/**",
                                "/api/order-items/**"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "DELIVERY_PARTNER",
                                "ADMIN"
                        )

                        // Everything else requires authentication.
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
