package com.example.crmservice.impl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_SUBSCRIBER = "SUBSCRIBER";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Manager user
        UserDetails manager = User.builder()
                .username("manager")
                .password(passwordEncoder.encode("managerPass123"))
                .roles(ROLE_MANAGER)
                .build();

        // Example subscriber (for testing basic auth with a known subscriber)
        // In a real scenario, subscriber authentication would likely involve a database lookup
        // or a more dynamic way to create UserDetails based on MSISDN.
        UserDetails subscriber1 = User.builder()
                .username("79001112233") // MSISDN as username
                .password(passwordEncoder.encode("subscriberPass")) // Example password
                .roles(ROLE_SUBSCRIBER)
                .build();

        // For dynamic subscriber authentication based on MSISDN from BRT, a custom UserDetailsService would be needed.
        // For now, using InMemoryUserDetailsManager for simplicity.
        return new InMemoryUserDetailsManager(manager, subscriber1);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless API
            .authorizeHttpRequests(authz -> authz
                // Manager endpoints
                .requestMatchers("/api/manager/**").hasRole(ROLE_MANAGER)
                // Subscriber endpoints
                .requestMatchers(HttpMethod.POST, "/api/subscriber/payment").hasRole(ROLE_SUBSCRIBER)
                // Potentially other subscriber-specific GET endpoints if they need to view their own data
                // .requestMatchers(HttpMethod.GET, "/api/subscriber/info").hasRole(ROLE_SUBSCRIBER) 
                // Public/other endpoints (if any)
                .anyRequest().authenticated() // All other requests need authentication
            )
            .httpBasic(Customizer.withDefaults()) // Use HTTP Basic authentication
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Stateless API

        return http.build();
    }
} 