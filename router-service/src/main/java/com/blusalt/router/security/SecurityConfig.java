package com.blusalt.router.security;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwt;

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilter() {
        var fr = new FilterRegistrationBean<JwtAuthFilter>(jwt);
        fr.addUrlPatterns("/api/*");
        fr.setOrder(1);
        return fr;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disable CSRF for APIs
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // allow all requests
                )
                .httpBasic(httpBasic -> httpBasic.disable()) // disable basic auth
                .formLogin(form -> form.disable()); // disable login form

        return http.build();
    }

}
