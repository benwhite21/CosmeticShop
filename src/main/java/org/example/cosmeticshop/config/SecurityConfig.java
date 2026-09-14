package org.example.cosmeticshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Cho phép tất cả giao diện HTML (Thymeleaf) & Tài nguyên tĩnh
                        .requestMatchers(
                                "/",
                                "/home",
                                "/login",
                                "/products/**",
                                "/cart/**",
                                "/checkout/**",
                                "/payment/**",
                                "/admin/**",
                                "/orders/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        // 2. Swagger UI, Uploads & Callback VNPAY
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/uploads/**",
                                "/api/payment/vnpay-callback"
                        ).permitAll()

                        // 3. API xác thực công khai
                        .requestMatchers("/api/auth/**").permitAll()

                        // 4. API đọc dữ liệu sản phẩm / danh mục công khai
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/brands/**",
                                "/api/reviews/**"
                        ).permitAll()

                        // 5. API đơn hàng & giỏ hàng: cho phép người dùng/admin có token thực thi
                        .requestMatchers("/api/orders/**", "/api/cart/**").authenticated()

                        // 6. API quản trị hệ thống
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 7. Quy tắc cuối cùng
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}