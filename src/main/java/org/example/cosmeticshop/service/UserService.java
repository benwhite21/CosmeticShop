package org.example.cosmeticshop.service;

import org.example.cosmeticshop.dto.LoginRequest;
import org.example.cosmeticshop.dto.RegisterRequest;
import org.example.cosmeticshop.entity.Role;
import org.example.cosmeticshop.entity.User;
import org.example.cosmeticshop.repository.RoleRepository;
import org.example.cosmeticshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserService {

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private RoleRepository roleRepository;

    public UserService() {}

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public User register(String fullName, String email, String phone, String password) {
        return register(new RegisterRequest(fullName, email, phone, password));
    }

    @Transactional
    public User register(RegisterRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        if (userRepository != null && userRepository.existsByEmail(req.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        Role customerRole = null;
        if (roleRepository != null) {
            customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_CUSTOMER")));
        }

        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail().trim());
        user.setPhone(req.getPhone());
        user.setPassword(req.getPassword());
        user.setActive(true);
        if (customerRole != null) {
            user.setRoles(Set.of(customerRole));
        }

        return userRepository != null ? userRepository.save(user) : user;
    }

    public User login(String email, String password) {
        return login(new LoginRequest(email, password));
    }

    public User login(LoginRequest req) {
        if (userRepository == null) {
            throw new IllegalStateException("UserRepository chưa được khởi tạo");
        }

        User user = userRepository.findByEmail(req.getEmail().trim())
                .orElseThrow(() -> new IllegalArgumentException("Sai tài khoản hoặc mật khẩu"));

        if (!user.getPassword().equals(req.getPassword())) {
            throw new IllegalArgumentException("Sai tài khoản hoặc mật khẩu");
        }

        if (!user.isActive()) {
            throw new IllegalStateException("Tài khoản của bạn đã bị khóa");
        }

        return user;
    }

    public User findById(Long id) {
        if (userRepository == null) {
            throw new IllegalStateException("UserRepository chưa được khởi tạo");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));
    }
}