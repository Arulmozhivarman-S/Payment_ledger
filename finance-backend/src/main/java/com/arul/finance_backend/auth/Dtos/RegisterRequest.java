package com.arul.finance_backend.auth.Dtos;

import com.arul.finance_backend.ledger.enums.UserRole;

public record RegisterRequest( String UserName, String email, String Password, UserRole role ) {}
