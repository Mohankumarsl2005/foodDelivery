package com.foodDelivery.config;

import com.foodDelivery.user.User;
import com.foodDelivery.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class AdminAccountInitializer {

    @Bean
    public CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            System.out.println("Checking ADMIN account...");

            boolean adminExists = userRepository.findAll()
                    .stream()
                    .anyMatch(user ->
                            user.getRole() == User.Role.ADMIN);

            if (!adminExists) {
                User admin = new User();
                admin.setName("System Admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(
                        passwordEncoder.encode("ChangeThisAdmin123!")
                );
                admin.setRole(User.Role.ADMIN);
                admin.setEnabled(true);

                userRepository.save(admin);

                System.out.println("Initial ADMIN account created.");
            } else {
                System.out.println("ADMIN account already exists.");
            }
        };
    }
}