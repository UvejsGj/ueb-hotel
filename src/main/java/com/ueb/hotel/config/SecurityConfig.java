package com.ueb.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.http.HttpStatus;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/access-denied", "/error", "/css/**", "/js/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/app/**", "/api/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/app/reservations", true)
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
                // për /api/** kthe 401 në vend të ridrejtimit te faqja e login-it
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    PathPatternRequestMatcher.pathPattern("/api/**")));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /** Përdoruesit in-memory (faza 1). Fjalëkalimet janë vetëm për zhvillim, shih README. */
    @Bean
    UserDetailsService users(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
            User.withUsername("recepsion")
                .password(encoder.encode("recepsion123"))
                .roles("USER")
                .build(),
            User.withUsername("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build()
        );
    }
}
