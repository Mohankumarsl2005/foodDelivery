
package com.foodDelivery.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(request.password())
        );
        user.setPhoneNumber(request.phoneNumber());

        // Never accept role or enabled from the registration request.
        user.setRole(User.Role.CUSTOMER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserByEmail(String email) {
        return userRepository.findByEmail(
                        email.trim().toLowerCase()
                )
                .map(UserResponse::from);
    }
}
