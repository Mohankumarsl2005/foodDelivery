
package com.foodDelivery.user;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phoneNumber,
        User.Role role,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
