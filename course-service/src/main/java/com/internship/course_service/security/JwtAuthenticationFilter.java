package com.internship.course_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        //se ia tokinul din request
        final String authHeader = request.getHeader("Authorization");

        //verifica daca exista token, daca nu, nu se blocheaza, pe urma se decide daca e public/protejat
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //se scoate token curat de 7 caractere, si username-ul
        final String jwt = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);

        //se verifica daca nu e user autentificat
        if (username != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            //cauta user in bd
            UserDetails userDetails =
                    customUserDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );
                //aici spring security afla, pentru acest request, userul este x
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
        }
        //requestul este trimis spre controller
        filterChain.doFilter(request, response);
    }
}