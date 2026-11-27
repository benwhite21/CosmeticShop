package org.example.cosmeticshop.service;

import org.example.cosmeticshop.dto.LoginRequest;
import org.example.cosmeticshop.dto.OrderRequest;
import org.example.cosmeticshop.dto.RegisterRequest;
import org.example.cosmeticshop.entity.*;
import org.example.cosmeticshop.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CosmeticShopUnitTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CartRepository cartRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private CouponRepository couponRepository;

    @InjectMocks private UserService userService;
    @InjectMocks private ProductService productService;
    @InjectMocks private CartService cartService;
    @InjectMocks private OrderService orderService;

    // ==================== 1. f_auth_login - UserService.login (5 TCs) ====================
    @Test @DisplayName("UTCID01 - Đăng nhập với Email null")
    void testLogin_EmailNull() {
        assertThrows(NullPointerException.class,
                () -> userService.login(null, "123456"));
    }

    @Test @DisplayName("UTCID02 - Đăng nhập với Email rỗng")
    void testLogin_EmailEmpty() {
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> userService.login("", "123456"));
    }

    @Test @DisplayName("UTCID03 - Đăng nhập với Email không tồn tại")
    void testLogin_EmailNotFound() {
        when(userRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> userService.login("notfound@gmail.com", "123456"));
    }

    @Test @DisplayName("UTCID04 - Đăng nhập sai mật khẩu")
    void testLogin_WrongPassword() {
        User user = new User();
        user.setEmail("customer@gmail.com");
        user.setPassword("123456");
        user.setActive(true);

        when(userRepository.findByEmail("customer@gmail.com")).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class,
                () -> userService.login("customer@gmail.com", "saipass"));
    }

    @Test @DisplayName("UTCID05 - Đăng nhập thành công")
    void testLogin_Success() {
        User user = new User();
        user.setEmail("customer@gmail.com");
        user.setPassword("123456");
        user.setActive(true);

        when(userRepository.findByEmail("customer@gmail.com")).thenReturn(Optional.of(user));
        User loggedIn = userService.login(new LoginRequest("customer@gmail.com", "123456"));
        assertEquals("customer@gmail.com", loggedIn.getEmail());
    }

    // ==================== 2. f_user_register - UserService.register (5 TCs) ====================
    @Test @DisplayName("UTCID06 - RegisterRequest null")
    void testRegister_NullRequest() {
        assertThrows(NullPointerException.class, () -> userService.register((RegisterRequest) null));
    }

    @Test @DisplayName("UTCID07 - Đăng ký với Email đã tồn tại")
    void testRegister_DuplicateEmail() {
        RegisterRequest req = new RegisterRequest("Nguyen Van A", "existed@gmail.com", "0912345678", "123456");
        when(userRepository.existsByEmail("existed@gmail.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test @DisplayName("UTCID08 - Đăng ký bỏ trống Email")
    void testRegister_EmptyEmail() {
        RegisterRequest req = new RegisterRequest("Nguyen Van A", "", "0912345678", "123456");
        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test @DisplayName("UTCID09 - Đăng ký bỏ trống Mật khẩu")
    void testRegister_EmptyPassword() {
        RegisterRequest req = new RegisterRequest("Nguyen Van A", "newuser@gmail.com", "0912345678", "");
        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test @DisplayName("UTCID10 - Đăng ký tài khoản hợp lệ thành công")
    void testRegister_Success() {
        RegisterRequest req = new RegisterRequest("Nguyen Van A", "newuser@gmail.com", "0912345678", "12345678");
        Role role = new Role("ROLE_CUSTOMER");

        when(userRepository.existsByEmail("newuser@gmail.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User saved = userService.register(req);
        assertNotNull(saved.getId());
        assertEquals("newuser@gmail.com", saved.getEmail());
    }

    // ==================== 3. ProductService (5 TCs) ====================
    @Test @DisplayName("UTCID11 - Thêm sản phẩm với giá âm")
    void testAddProduct_NegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.addProduct("Son môi", "son-moi", "Mô tả", -10000, 10, "Son"));
    }

    @Test @DisplayName("UTCID12 - Thêm sản phẩm với giá biên = 0")
    void testAddProduct_ZeroPrice() {
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        Product p = productService.addProduct("Quà tặng", "qua-tang", "Mô tả", 0, 5, "Quà");
        assertEquals(BigDecimal.valueOf(0.0), p.getPrice());
    }

    @Test @DisplayName("UTCID13 - Thêm sản phẩm hợp lệ thành công")
    void testAddProduct_Success() {
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        Product p = productService.addProduct("Kem chống nắng Anessa", "anessa", "Chống nắng", 200000, 15, "Skincare");
        assertEquals("Kem chống nắng Anessa", p.getName());
        assertEquals(15, p.getStock());
    }

    @Test @DisplayName("UTCID14 - Thêm sản phẩm với tồn kho âm")
    void testAddProduct_NegativeStock() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.addProduct("Sữa rửa mặt", "srm", "Mô tả", 150000, -1, "Skincare"));
    }

    @Test @DisplayName("UTCID15 - Tìm kiếm sản phẩm theo ID không tồn tại")
    void testGetProductById_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> productService.getProductById(999L));
    }

    // ==================== 4. f_cart_checkStock - CartService.addToCart (5 TCs) ====================
    @Test @DisplayName("UTCID16 - Thêm vào giỏ số lượng = 0")
    void testAddToCart_ZeroQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart(1L, 1L, 0));
    }

    @Test @DisplayName("UTCID17 - Thêm vào giỏ số lượng = 1 (biên dưới)")
    void testAddToCart_MinBoundary() {
        Product p = new Product();
        p.setId(1L);
        p.setStock(5);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart updated = cartService.addToCart(1L, 1L, 1);
        assertEquals(1, updated.getItems().size());
    }

    @Test @DisplayName("UTCID18 - Thêm vào giỏ số lượng = 3 (bình thường)")
    void testAddToCart_NormalQuantity() {
        Product p = new Product();
        p.setId(1L);
        p.setStock(5);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart updated = cartService.addToCart(1L, 1L, 3);
        assertEquals(3, updated.getItems().get(0).getQuantity());
    }

    @Test @DisplayName("UTCID19 - Thêm vào giỏ số lượng = 5 (bằng tồn kho)")
    void testAddToCart_MaxBoundary() {
        Product p = new Product();
        p.setId(1L);
        p.setStock(5);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart updated = cartService.addToCart(1L, 1L, 5);
        assertEquals(5, updated.getItems().get(0).getQuantity());
    }

    @Test @DisplayName("UTCID20 - Thêm vào giỏ số lượng = 6 (vượt tồn kho)")
    void testAddToCart_ExceedStock() {
        Product p = new Product();
        p.setId(1L);
        p.setStock(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart(1L, 1L, 6));
    }

    // ==================== 5. OrderService (5 TCs) ====================
    @Test @DisplayName("UTCID21 - Tạo đơn hàng khi giỏ hàng rỗng")
    void testCreateOrder_EmptyItems() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(1L, "Khánh", "0912345678", "Hà Nội", PaymentMethod.COD, Collections.emptyList()));
    }

    @Test @DisplayName("UTCID22 - Đặt hàng vượt quá tồn kho")
    void testCreateOrder_InsufficientStock() {
        Product p = new Product();
        p.setId(1L);
        p.setPrice(BigDecimal.valueOf(200000));
        p.setStock(2);

        CartItem item = new CartItem();
        item.setProduct(p);
        item.setQuantity(5);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(1L, "Khánh", "0912345678", "Hà Nội", PaymentMethod.COD, List.of(item)));
    }

    @Test @DisplayName("UTCID23 - Đặt hàng COD thành công và trừ tồn kho")
    void testCreateOrder_Success() {
        Product p = new Product();
        p.setId(1L);
        p.setPrice(BigDecimal.valueOf(200000));
        p.setStock(10);

        CartItem item = new CartItem();
        item.setProduct(p);
        item.setQuantity(2);

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.createOrder(1L, "Khánh", "0912345678", "Hà Nội", PaymentMethod.COD, List.of(item));
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(BigDecimal.valueOf(400000), order.getTotalAmount());
        assertEquals(8, p.getStock());
    }

    @Test @DisplayName("UTCID24 - Hủy đơn hàng hoàn trả lại tồn kho")
    void testCancelOrder_RestoreStock() {
        Product p = new Product();
        p.setId(1L);
        p.setStock(8);

        OrderItem oi = new OrderItem();
        oi.setProduct(p);
        oi.setQuantity(2);

        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(List.of(oi));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order updated = orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, updated.getStatus());
        assertEquals(10, p.getStock());
    }

    @Test @DisplayName("UTCID25 - Đặt hàng áp dụng mã giảm giá không tồn tại")
    void testPlaceOrder_InvalidCoupon() {
        User user = new User();
        user.setId(1L);
        user.setFullName("Khánh");

        Product p = new Product();
        p.setId(1L);
        p.setName("Son");
        p.setPrice(BigDecimal.valueOf(200000));
        p.setStock(5);

        CartItem ci = new CartItem();
        ci.setProduct(p);
        ci.setQuantity(1);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(ci)));

        OrderRequest req = new OrderRequest();
        req.setShippingAddress("Hà Nội");
        req.setCouponCode("GIAM50K_SAI");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(couponRepository.findByCodeIgnoreCase("GIAM50K_SAI")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(1L, req));
    }
}