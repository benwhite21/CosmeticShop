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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private RoleRepository roleRepository;

    // Bộ nhớ tạm phục vụ Unit Test khi chưa kết nối DB (giống ProductService, CartService)
    private final List<User> memoryUsers = new ArrayList<>();
    private final AtomicLong userIdGen = new AtomicLong(1);

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
        if (req == null) {
            throw new IllegalArgumentException("Thông tin đăng ký không được để trống");
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        String cleanEmail = req.getEmail().trim();
        if (!cleanEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email không đúng định dạng");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        if (req.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        // Kiểm tra trùng email (cả trong DB lẫn bộ nhớ tạm)
        if (userRepository != null) {
            if (userRepository.existsByEmail(cleanEmail)) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
        } else {
            boolean existsInMemory = memoryUsers.stream()
                    .anyMatch(u -> u.getEmail().equalsIgnoreCase(cleanEmail));
            if (existsInMemory) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
        }

        Role customerRole = null;
        if (roleRepository != null) {
            customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_CUSTOMER")));
        }

        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(cleanEmail);
        user.setPhone(req.getPhone());
        user.setPassword(req.getPassword());
        user.setActive(true);
        if (customerRole != null) {
            user.setRoles(Set.of(customerRole));
        }

        if (userRepository != null) {
            return userRepository.save(user);
        } else {
            user.setId(userIdGen.getAndIncrement());
            memoryUsers.add(user);
            return user;
        }
    }

    public User login(String email, String password) {
        return login(new LoginRequest(email, password));
    }

    public User login(LoginRequest req) {
        if (req == null || req.getEmail() == null || req.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        String cleanEmail = req.getEmail().trim();

        User user;
        if (userRepository != null) {
            user = userRepository.findByEmail(cleanEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Sai tài khoản hoặc mật khẩu"));
        } else {
            user = memoryUsers.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(cleanEmail))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Sai tài khoản hoặc mật khẩu"));
        }

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
            return memoryUsers.stream()
                    .filter(u -> u.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));
    }
}