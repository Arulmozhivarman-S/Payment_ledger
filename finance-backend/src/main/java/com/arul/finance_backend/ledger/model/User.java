package com.arul.finance_backend.ledger.model;

import com.arul.finance_backend.ledger.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    Long userId;

    @NotBlank
    String userName;

    @NotBlank
    @Column( unique = true )
    @Email( message = "Please provide a valid email" )
    String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @NotBlank
    @Size( min=8 )
    @Column( nullable = false )
    String password;

}
