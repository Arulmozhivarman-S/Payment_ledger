package com.arul.finance_backend.auth.controller;

import org.springframework.web.bind.annotation.RestController;

import com.arul.finance_backend.auth.Dtos.AuthResponse;
import com.arul.finance_backend.auth.Dtos.RegisterRequest;
import com.arul.finance_backend.auth.Dtos.RegisterResponse;
import com.arul.finance_backend.auth.Dtos.UserDto;
import com.arul.finance_backend.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDto userDto) {
        String token = authService.login(userDto);
        
        if(token==null) return ResponseEntity.badRequest().body("Invalid UserName or Password");
        return ResponseEntity.ok(new AuthResponse(token));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        String token = authService.register(registerRequest);
        return ResponseEntity.ok(new RegisterResponse(token));
    }
}
