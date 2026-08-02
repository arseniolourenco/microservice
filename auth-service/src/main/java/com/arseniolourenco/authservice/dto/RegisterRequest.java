package com.arseniolourenco.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(

    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Password is required")
    String password,

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    String email,

    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName
) {
}
