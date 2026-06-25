package com.arul.finance_backend.auth.Dtos;

import com.arul.finance_backend.ledger.enums.UserRole;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDto( 
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    String email, 
    
    @NotBlank
    @Enumerated(EnumType.STRING)
    UserRole role,

    @NotBlank(message = "Password cannot be empty")
    String password
) {}
