package com.cacttus.hotel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${app.user.password}")
    private String userPassword;

    @Value("${app.admin.password}")
    private String adminPassword;

    // Një përdorues për secilin anëtar të grupit (faza 1: checkpoint-i, secili bën login live).
    @Value("${app.uvejs.password}")
    private String uvejsPassword;

    @Value("${app.muzlind.password}")
    private String muzlindPassword;

    @Value("${app.xhemaledin.password}")
    private String xhemaledinPassword;

    @Value("${app.dion.password}")
    private String dionPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode(userPassword))
                .roles("USER")
                .build();
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();
        UserDetails uvejs = User.withUsername("uvejs")
                .password(passwordEncoder.encode(uvejsPassword))
                .roles("ADMIN")
                .build();
        UserDetails muzlind = User.withUsername("muzlind")
                .password(passwordEncoder.encode(muzlindPassword))
                .roles("ADMIN")
                .build();
        UserDetails xhemaledin = User.withUsername("xhemaledin")
                .password(passwordEncoder.encode(xhemaledinPassword))
                .roles("ADMIN")
                .build();
        UserDetails dion = User.withUsername("dion")
                .password(passwordEncoder.encode(dionPassword))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user, admin, uvejs, muzlind, xhemaledin, dion);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/error/**").permitAll()
                        // Leximi pa login: listat dhe faqja e dhomës, vetëm me GET.
                        // "/app/rooms/*" është vetëm një segment, p.sh. /app/rooms/1 (jo /app/rooms/1/edit).
                        .requestMatchers(HttpMethod.GET, "/", "/app/reservations", "/app/rooms", "/app/rooms/*").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/app/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        // Pa "true": pas login-it të kthen te faqja që kërkove (p.sh. "Rezervim i ri").
                        .defaultSuccessUrl("/app/reservations")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }
}
