package org.example.cosmeticshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.cosmeticshop.dto.AuthResponse;
import org.example.cosmeticshop.dto.LoginRequest;
import org.example.cosmeticshop.entity.Role;
import org.example.cosmeticshop.entity.User;
import org.example.cosmeticshop.repository.RoleRepository;
import org.example.cosmeticshop.repository.UserRepository;
import org.example.cosmeticshop.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "API Đăng nhập, Đăng ký và Xác thực JWT")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Đăng nhập tài khoản, nhận Token JWT")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);

        if (user == null || (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())
                && !loginRequest.getPassword().equals(user.getPassword()))) {
            return ResponseEntity.badRequest().body("Email hoặc mật khẩu không chính xác!");
        }

        String role = user.getPrimaryRoleName();
        String token = jwtService.generateToken(user.getEmail(), role);

        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), role));
    }

    @Operation(summary = "Đăng ký tài khoản người dùng mới")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email đã tồn tại trên hệ thống!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                    .or(() -> roleRepository.findByName("CUSTOMER"))
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_CUSTOMER")));
            user.getRoles().add(customerRole);
        }
        user.setActive(true);

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }
}