package com.arul.finance_backend.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.arul.finance_backend.auth.Dtos.RegisterRequest;
import com.arul.finance_backend.auth.Dtos.UserDto;
import com.arul.finance_backend.auth.jwt.JwtUtills;
import com.arul.finance_backend.ledger.model.User;
import com.arul.finance_backend.ledger.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtills jwtUtills;

    public String login(@RequestBody UserDto userDto ){  
        User user = userRepository.findByEmail(userDto.email()).orElseThrow();

        if(passwordEncoder.matches(userDto.password(), user.getPassword() ) ){
            String token = jwtUtills.getToken(user.getUserName(), userDto.role());
            return token;
        }
        else return null;
    }

    public String register(@RequestBody RegisterRequest registerRequest){
        String encryptedPassword = passwordEncoder.encode(registerRequest.Password());
        String token = jwtUtills.getToken(registerRequest.UserName(), registerRequest.role());
        
        User newUser = User.builder()
            .userName(registerRequest.UserName())
            .email(registerRequest.email())
            .userRole(registerRequest.role())
            .password(encryptedPassword)
            .build(); 
        userRepository.save(newUser);
        return token;
    }

}
