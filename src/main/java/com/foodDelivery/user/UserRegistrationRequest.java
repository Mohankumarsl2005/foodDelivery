package com.foodDelivery.user;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        @NotBlank
        @Email
        @Size(max = 100)
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String password,

        @Size(max = 20)
        String phoneNumber
) {
}

