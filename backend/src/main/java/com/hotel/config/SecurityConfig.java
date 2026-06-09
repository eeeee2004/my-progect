package com.hotel.config;

import com.hotel.common.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtils jwtUtils;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/guest/**").hasAuthority("GUEST")
                .requestMatchers("/api/front-desk/**").hasAuthority("FRONT_DESK")
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/room-types/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OncePerRequestFilter jwtAuthenticationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    if (jwtUtils.validateToken(token)) {
                        var claims = jwtUtils.parseToken(token);
                        String roleType = claims.get("roleType", String.class);
                        Long userId = claims.get("userId", Long.class);
                        if (userId == null) {
                            userId = claims.get("employeeId", Long.class);
                        }

                        var authorities = java.util.List.of(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(roleType)
                        );
                        var auth = new org.springframework.security.authentication
                            .UsernamePasswordAuthenticationToken(userId, null, authorities);
                        org.springframework.security.core.context.SecurityContextHolder.getContext()
                            .setAuthentication(auth);
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
    }
}
