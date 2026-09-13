package org.example.cosmeticshop.config;

import org.example.cosmeticshop.entity.*;
import org.example.cosmeticshop.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           BrandRepository brandRepository,
                           ProductRepository productRepository,
                           CouponRepository couponRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
    }

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_CUSTOMER")));

        if (!userRepository.existsByEmail("admin@cosmetic.com")) {
            User admin = new User();
            admin.setEmail("admin@cosmetic.com");
            admin.setPassword("admin123");
            admin.setFullName("Quản Trị Viên");
            admin.setPhone("0900000001");
            admin.setActive(true);
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
            System.out.println(">> Đã khởi tạo tài khoản Admin: admin@cosmetic.com / admin123");
        }

        if (!userRepository.existsByEmail("customer@gmail.com")) {
            User customer = new User();
            customer.setEmail("customer@gmail.com");
            customer.setPassword("customer123");
            customer.setFullName("Nguyễn Thị Lan");
            customer.setPhone("0987654321");
            customer.setActive(true);
            customer.setRoles(Set.of(customerRole));
            userRepository.save(customer);
            System.out.println(">> Đã khởi tạo tài khoản Customer: customer@gmail.com / customer123");
        }

        Category catMakeup = categoryRepository.findByName("Trang điểm")
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setName("Trang điểm");
                    cat.setSlug("trang-diem");
                    cat.setDescription("Các dòng son môi, phấn nền, chì kẻ mày cao cấp");
                    return categoryRepository.save(cat);
                });

        Category catSkincare = categoryRepository.findByName("Chăm sóc da")
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setName("Chăm sóc da");
                    cat.setSlug("cham-soc-da");
                    cat.setDescription("Kem dưỡng ẩm, serum, sữa rửa mặt trị mụn");
                    return categoryRepository.save(cat);
                });

        Brand brandMac = brandRepository.findByName("M.A.C")
                .orElseGet(() -> {
                    Brand b = new Brand();
                    b.setName("M.A.C");
                    b.setSlug("mac");
                    return brandRepository.save(b);
                });

        Brand brandLaRoche = brandRepository.findByName("La Roche-Posay")
                .orElseGet(() -> {
                    Brand b = new Brand();
                    b.setName("La Roche-Posay");
                    b.setSlug("la-roche-posay");
                    return brandRepository.save(b);
                });

        if (productRepository.count() == 0) {
            Product p1 = new Product();
            p1.setName("Son môi M.A.C Chili Matte");
            p1.setSlug("son-moi-mac-chili-matte");
            p1.setPrice(BigDecimal.valueOf(450000.00));
            p1.setStock(30);
            p1.setStatus(ProductStatus.ACTIVE);
            p1.setDescription("Son thỏi lì M.A.C màu Chili đỏ gạch thời thượng tôn da.");
            p1.setCategory(catMakeup);
            p1.setBrand(brandMac);
            p1.setCreatedAt(LocalDateTime.now());
            p1.setUpdatedAt(LocalDateTime.now());
            productRepository.save(p1);

            Product p2 = new Product();
            p2.setName("Kem dưỡng La Roche-Posay B5+");
            p2.setSlug("kem-duong-la-roche-posay-b5-plus");
            p2.setPrice(BigDecimal.valueOf(320000.00));
            p2.setStock(25);
            p2.setStatus(ProductStatus.ACTIVE);
            p2.setDescription("Kem dưỡng phục hồi da Cicaplast Baume B5+ dịu nhẹ.");
            p2.setCategory(catSkincare);
            p2.setBrand(brandLaRoche);
            p2.setCreatedAt(LocalDateTime.now());
            p2.setUpdatedAt(LocalDateTime.now());
            productRepository.save(p2);

            Product p3 = new Product();
            p3.setName("Sữa rửa mặt La Roche-Posay Effaclar");
            p3.setSlug("sua-rua-mat-la-roche-posay-effaclar");
            p3.setPrice(BigDecimal.valueOf(380000.00));
            p3.setStock(50);
            p3.setStatus(ProductStatus.ACTIVE);
            p3.setDescription("Gel rửa mặt tạo bọt làm sạch sâu và giảm dầu nhờn.");
            p3.setCategory(catSkincare);
            p3.setBrand(brandLaRoche);
            p3.setCreatedAt(LocalDateTime.now());
            p3.setUpdatedAt(LocalDateTime.now());
            productRepository.save(p3);

            System.out.println(">> Đã khởi tạo 3 sản phẩm mẫu vào cơ sở dữ liệu MySQL.");
        }

        if (couponRepository.count() == 0) {
            Coupon c1 = new Coupon();
            c1.setCode("CHAOBANMOI");
            c1.setDiscountType(DiscountType.PERCENT);
            c1.setDiscountValue(BigDecimal.valueOf(10));
            c1.setMinOrderAmount(BigDecimal.valueOf(200000));
            c1.setMaxDiscountAmount(BigDecimal.valueOf(50000));
            c1.setQuantity(100);
            c1.setStartDate(LocalDateTime.now().minusDays(1));
            c1.setEndDate(LocalDateTime.now().plusMonths(1));
            c1.setActive(true);
            couponRepository.save(c1);

            Coupon c2 = new Coupon();
            c2.setCode("SALE50K");
            c2.setDiscountType(DiscountType.FIXED_AMOUNT);
            c2.setDiscountValue(BigDecimal.valueOf(50000));
            c2.setMinOrderAmount(BigDecimal.valueOf(300000));
            c2.setQuantity(50);
            c2.setStartDate(LocalDateTime.now().minusDays(1));
            c2.setEndDate(LocalDateTime.now().plusMonths(1));
            c2.setActive(true);
            couponRepository.save(c2);

            System.out.println(">> Đã khởi tạo 2 mã giảm giá mẫu: CHAOBANMOI và SALE50K");
        }
    }
}