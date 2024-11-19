package org.course.config;

import org.course.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/users").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/orders/{size}/page/{page}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/orders").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/orders/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/orders/{id}").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/dishes/{size}/page/{page}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/dishes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/dishes/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/dishes/{id}").hasRole("ADMIN")

                .requestMatchers(HttpMethod.PUT, "/api/reviews/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/{id}").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/tables/{id}/reserve").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/tables").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tables/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/tables/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/tables/{id}/reserve").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .httpBasic();
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
