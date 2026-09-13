package org.example.cosmeticshop.service;

import org.example.cosmeticshop.entity.*;
import org.example.cosmeticshop.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CosmeticShopServiceTest {

    private UserService userService;
    private ProductService productService;
    private CartService cartService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        productService = new ProductService();
        cartService = new CartService();
        orderService = new OrderService(productService);
    }

    // ==========================================
    // LUỒNG 1: ĐĂNG KÝ TÀI KHOẢN
    // ==========================================
    @Nested
    @DisplayName("Tests for User Registration")
    class RegistrationTests {
        @Test
        @DisplayName("Đăng ký thành công với thông tin hợp lệ")
        void testRegisterSuccess() {
            User user = userService.register("Nguyễn Văn A", "vana@example.com", "0987654321", "password123");
            assertNotNull(user);
            assertEquals("Nguyễn Văn A", user.getFullName());
            assertEquals("vana@example.com", user.getEmail());
        }

        @Test
        @DisplayName("Đăng ký thất bại khi email đã tồn tại")
        void testRegisterDuplicateEmail() {
            userService.register("Nguyễn Văn A", "vana@example.com", "0987654321", "password123");
            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    userService.register("Nguyễn Văn B", "vana@example.com", "0912345678", "password456")
            );
            assertEquals("Email đã tồn tại", exception.getMessage());
        }

        @Test
        @DisplayName("Đăng ký thất bại khi email sai định dạng")
        void testRegisterInvalidEmail() {
            assertThrows(IllegalArgumentException.class, () ->
                    userService.register("Nguyễn Văn A", "invalid-email", "0987654321", "password123")
            );
        }

        @Test
        @DisplayName("Đăng ký thất bại khi mật khẩu quá ngắn (< 6 ký tự)")
        void testRegisterShortPassword() {
            assertThrows(IllegalArgumentException.class, () ->
                    userService.register("Nguyễn Văn A", "vana@example.com", "0987654321", "123")
            );
        }
    }

    // ==========================================
    // LUỒNG 2: ĐĂNG NHẬP
    // ==========================================
    @Nested
    @DisplayName("Tests for User Login")
    class LoginTests {
        @BeforeEach
        void initUser() {
            userService.register("Trần Thị B", "thib@example.com", "0981112233", "pass123456");
        }

        @Test
        @DisplayName("Đăng nhập thành công")
        void testLoginSuccess() {
            User user = userService.login("thib@example.com", "pass123456");
            assertNotNull(user);
            assertEquals("thib@example.com", user.getEmail());
        }

        @Test
        @DisplayName("Đăng nhập thất bại khi sai mật khẩu")
        void testLoginWrongPassword() {
            assertThrows(IllegalArgumentException.class, () ->
                    userService.login("thib@example.com", "wrongpass")
            );
        }
    }

    // ==========================================
    // LUỒNG 3 & 6: SẢN PHẨM & TÌM KIẾM
    // ==========================================
    @Nested
    @DisplayName("Tests for Product & Filter")
    class ProductTests {
        @Test
        @DisplayName("Thêm và tìm kiếm/lọc sản phẩm")
        void testProductSearchAndFilter() {
            productService.addProduct("Son môi XYZ", "Son", "ABC", 150000.0, 20, "Son lì");
            productService.addProduct("Kem dưỡng ABC", "Skincare", "ABC", 250000.0, 15, "Kem ẩm");

            List<Product> searchByName = productService.searchAndFilter("Son", null, null, null);
            assertEquals(1, searchByName.size());
            assertEquals("Son môi XYZ", searchByName.get(0).getName());

            List<Product> filterByPrice = productService.searchAndFilter(null, null, 200000.0, 300000.0);
            assertEquals(1, filterByPrice.size());
            assertEquals("Kem dưỡng ABC", filterByPrice.get(0).getName());
        }

        @Test
        @DisplayName("Thêm sản phẩm giá <= 0 báo lỗi")
        void testAddProductInvalidPrice() {
            assertThrows(IllegalArgumentException.class, () ->
                    productService.addProduct("Lỗi", "Son", "ABC", -5000.0, 10, "Mô tả")
            );
        }
    }

    // ==========================================
    // LUỒNG 4: GIỎ HÀNG
    // ==========================================
    @Nested
    @DisplayName("Tests for Shopping Cart")
    class CartTests {
        @Test
        @DisplayName("Thêm sản phẩm vào giỏ và tính tổng tiền chính xác")
        void testAddToCartAndCalculateTotal() {
            Product p1 = productService.addProduct("Son môi", "Son", "ABC", 150000.0, 10, "");
            Product p2 = productService.addProduct("Kem dưỡng", "Skincare", "ABC", 250000.0, 10, "");

            cartService.addToCart(p1, 2); // 300,000
            cartService.addToCart(p2, 1); // 250,000

            assertEquals(2, cartService.getCartItems().size());
            assertEquals(550000.0, cartService.getTotalAmount());
        }

        @Test
        @DisplayName("Thêm vào giỏ vượt quá tồn kho báo lỗi")
        void testAddToCartExceedStock() {
            Product p = productService.addProduct("Son môi", "Son", "ABC", 150000.0, 5, "");
            assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(p, 10));
        }
    }

    // ==========================================
    // LUỒNG 5 & 7: ĐẶT HÀNG & DUYỆT ĐƠN
    // ==========================================
    @Nested
    @DisplayName("Tests for Order & Checkout")
    class OrderTests {
        @Test
        @DisplayName("Đặt hàng thành công và cập nhật trừ tồn kho")
        void testCreateOrderAndDeductStock() {
            Product p1 = productService.addProduct("Son XYZ", "Son", "ABC", 150000.0, 20, "");
            cartService.addToCart(p1, 2);

            Order order = orderService.createOrder(
                    1L,
                    "Lê Văn C",
                    "0909123456",
                    "123 Cầu Giấy, Hà Nội",
                    PaymentMethod.COD,
                    cartService.getCartItems()
            );

            assertNotNull(order);
            assertEquals(OrderStatus.PENDING, order.getStatus());
            assertEquals(300000.0, order.getTotalAmount());
            // Kiểm tra tồn kho đã giảm từ 20 -> 18
            assertEquals(18, productService.getProductById(p1.getId()).getStock());

            // Admin duyệt đơn hàng sang CONFIRMED
            Order updated = orderService.updateOrderStatus(order.getId(), OrderStatus.CONFIRMED);
            assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
        }

        @Test
        @DisplayName("Đặt hàng với giỏ hàng rỗng báo lỗi")
        void testCreateOrderEmptyCart() {
            assertThrows(IllegalArgumentException.class, () ->
                    orderService.createOrder(1L, "Lê Văn C", "0909123456", "Hà Nội", PaymentMethod.COD, List.of())
            );
        }
    }
}