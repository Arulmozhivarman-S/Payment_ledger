package com.arul.finance_backend.auth.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private final JwtUtills jwtUtills;

    @Override
    protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException{
        
        String header = request.getHeader("Authorization");

        if(header==null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if( jwtUtills.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication()==null ) {

            String userName = jwtUtills.extractUserName(token);
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + jwtUtills.extractRole(token)));

            var auth = new UsernamePasswordAuthenticationToken( userName, null, authorities);   // no roles yet

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

        }

        chain.doFilter(request, response);
    }
    
}
