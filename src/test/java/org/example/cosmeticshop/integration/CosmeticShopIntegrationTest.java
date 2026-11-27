package org.example.cosmeticshop.integration;

import org.example.cosmeticshop.dto.LoginRequest;
import org.example.cosmeticshop.dto.OrderRequest;
import org.example.cosmeticshop.dto.RegisterRequest;
import org.example.cosmeticshop.entity.Cart;
import org.example.cosmeticshop.entity.Order;
import org.example.cosmeticshop.entity.OrderStatus;
import org.example.cosmeticshop.entity.Product;
import org.example.cosmeticshop.entity.ProductStatus;
import org.example.cosmeticshop.entity.Role;
import org.example.cosmeticshop.entity.User;
import org.example.cosmeticshop.repository.OrderRepository;
import org.example.cosmeticshop.repository.ProductRepository;
import org.example.cosmeticshop.repository.RoleRepository;
import org.example.cosmeticshop.repository.UserRepository;
import org.example.cosmeticshop.service.CartService;
import org.example.cosmeticshop.service.OrderService;
import org.example.cosmeticshop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CosmeticShopIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserService userService;
    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;

    private Product sampleProduct;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        roleRepository.findByName("ROLE_CUSTOMER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_CUSTOMER")));

        sampleUser = userService.register(
                new RegisterRequest("Nguyễn Nam Khánh", "kh@gmail.com", "0987654321", "123456")
        );

        Product p = new Product();
        p.setName("Kem chống nắng Anessa");
        p.setSlug("kem-chong-nang-anessa");
        p.setPrice(BigDecimal.valueOf(350000));
        p.setStock(10);
        p.setStatus(ProductStatus.ACTIVE);
        sampleProduct = productRepository.save(p);
    }

    @Test
    @DisplayName("TC-INT-01: Tích hợp Đăng ký & Tự động gán quyền ROLE_CUSTOMER vào CSDL")
    void testRegisterIntegration_SaveToDatabaseAndAssignRole() {
        RegisterRequest req = new RegisterRequest("User Mới", "newuser@gmail.com", "0912345678", "123456");
        userService.register(req);

        User fromDb = userRepository.findByEmail("newuser@gmail.com").orElseThrow();
        assertNotNull(fromDb.getId());
        assertEquals("User Mới", fromDb.getFullName());
        assertTrue(fromDb.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_CUSTOMER")));
    }

    @Test
    @DisplayName("TC-INT-02: Tích hợp Xác thực đăng nhập & API Login")
    void testLoginIntegration_AuthenticateAgainstDatabase() throws Exception {
        LoginRequest loginReq = new LoginRequest("kh@gmail.com", "123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("TC-INT-03: Tích hợp Truy vấn & Lọc danh sách sản phẩm từ CSDL")
    void testProductFilterIntegration_QueryDatabase() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("keyword", "Anessa")
                        .param("minPrice", "200000")
                        .param("maxPrice", "500000"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-INT-04: Tích hợp Thêm vào giỏ hàng & Ràng buộc tồn kho CSDL")
    void testAddToCartIntegration_StockConstraint() {
        Cart cart = cartService.addToCart(sampleUser.getId(), sampleProduct.getId(), 3);
        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().getFirst().getQuantity());

        assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart(sampleUser.getId(), sampleProduct.getId(), 8));
    }

    @Test
    @DisplayName("TC-INT-05: Tích hợp Đặt hàng (Transaction) -> Lưu Order, Trừ kho & Xóa giỏ hàng")
    void testPlaceOrderIntegration_TransactionSync() {
        cartService.addToCart(sampleUser.getId(), sampleProduct.getId(), 2);

        OrderRequest orderReq = new OrderRequest();
        orderReq.setCustomerName("Nguyễn Nam Khánh");
        orderReq.setPhone("0987654321");
        orderReq.setShippingAddress("Nam Từ Liêm, Hà Nội");

        Order createdOrder = orderService.placeOrder(sampleUser.getId(), orderReq);

        Order orderInDb = orderRepository.findById(createdOrder.getId()).orElseThrow();
        Product productInDb = productRepository.findById(sampleProduct.getId()).orElseThrow();
        Cart cartInDb = cartService.getCartByUserId(sampleUser.getId());

        assertEquals(OrderStatus.PENDING, orderInDb.getStatus());
        assertEquals(BigDecimal.valueOf(700000), orderInDb.getFinalAmount());
        assertEquals(8, productInDb.getStock());
        assertTrue(cartInDb.getItems().isEmpty());
    }

    @Test
    @DisplayName("TC-INT-06: Tích hợp Hủy đơn hàng -> Cập nhật CANCELLED & Hoàn trả tồn kho CSDL")
    void testCancelOrderIntegration_RestoreStockInDatabase() {
        cartService.addToCart(sampleUser.getId(), sampleProduct.getId(), 3);
        OrderRequest orderReq = new OrderRequest();
        orderReq.setShippingAddress("Hà Nội");
        Order order = orderService.placeOrder(sampleUser.getId(), orderReq);

        assertEquals(7, productRepository.findById(sampleProduct.getId()).orElseThrow().getStock());

        orderService.updateOrderStatus(order.getId(), OrderStatus.CANCELLED);

        Order cancelledOrder = orderRepository.findById(order.getId()).orElseThrow();
        Product restoredProduct = productRepository.findById(sampleProduct.getId()).orElseThrow();

        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        assertEquals(10, restoredProduct.getStock());
    }

    @Test
    @DisplayName("TC-INT-07: Tích hợp Spring Security chặn truy cập trái phép vào /api/admin/**")
    void testSecurityIntegration_BlockUnauthorizedAdminAccess() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().is4xxClientError()); // Trả về 401 hoặc 403
    }

    @Test
    @DisplayName("TC-INT-08: Tích hợp Luồng dữ liệu từ Giỏ hàng sang Chi tiết đơn hàng (OrderItems)")
    void testOrderItemsIntegration_PersistLineItems() {
        cartService.addToCart(sampleUser.getId(), sampleProduct.getId(), 4);
        OrderRequest req = new OrderRequest();
        req.setShippingAddress("Hà Nội");

        Order savedOrder = orderService.placeOrder(sampleUser.getId(), req);
        Order fetched = orderRepository.findById(savedOrder.getId()).orElseThrow();

        assertEquals(1, fetched.getItems().size());
        assertEquals("Kem chống nắng Anessa", fetched.getItems().getFirst().getProductName());
        assertEquals(4, fetched.getItems().getFirst().getQuantity());
    }

    @Test
    @DisplayName("TC-INT-09: Tích hợp API /api/ai/consult đọc danh mục từ CSDL & trả về gợi ý theo loại da")
    void testAiConsultApiIntegration_WithSkinType() throws Exception {
        String payload = """
                {
                    "skinType": "Da hỗn hợp",
                    "message": "Mình muốn tìm kem chống nắng anessa"
                }
                """;

        mockMvc.perform(post("/api/ai/consult")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").isNotEmpty());
    }

    @Test
    @DisplayName("TC-INT-10: Tích hợp API /api/ai/consult xử lý khi khách không chọn loại da (skinType rỗng)")
    void testAiConsultApiIntegration_WithoutSkinType() throws Exception {
        String payload = """
                {
                    "skinType": "",
                    "message": "Tư vấn sản phẩm dưỡng da bán chạy"
                }
                """;

        mockMvc.perform(post("/api/ai/consult")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").isNotEmpty());
    }
}