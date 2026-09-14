package org.example.cosmeticshop.service;

import org.example.cosmeticshop.dto.OrderRequest;
import org.example.cosmeticshop.entity.*;
import org.example.cosmeticshop.repository.CouponRepository;
import org.example.cosmeticshop.repository.OrderRepository;
import org.example.cosmeticshop.repository.ProductRepository;
import org.example.cosmeticshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    @Autowired(required = false)
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private ProductRepository productRepository;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private CartService cartService;

    @Autowired(required = false)
    private CouponRepository couponRepository;

    private ProductService productService;
    private final List<Order> memoryOrders = new ArrayList<>();
    private final AtomicLong orderIdGen = new AtomicLong(1);

    public OrderService() {}

    public OrderService(ProductService productService) {
        this.productService = productService;
    }

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        CartService cartService,
                        CouponRepository couponRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.couponRepository = couponRepository;
    }

    // Lấy toàn bộ danh sách đơn hàng cho trang Admin Dashboard
    public List<Order> getAllOrders() {
        if (orderRepository != null) {
            return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }
        return memoryOrders;
    }

    public Order createOrder(long userId, String customerName, String phone, String shippingAddress,
                             PaymentMethod paymentMethod, List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất một sản phẩm");
        }

        Order order = new Order();
        order.setId(orderIdGen.getAndIncrement());
        order.setOrderCode("ORD-" + System.currentTimeMillis());
        order.setCustomerName(customerName);
        order.setPhone(phone);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : PaymentMethod.COD);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : items) {
            Product p = ci.getProduct();
            if (p != null) {
                if (p.getStock() != null && p.getStock() < ci.getQuantity()) {
                    throw new IllegalArgumentException("Sản phẩm không đủ tồn kho");
                }
                if (p.getStock() != null) {
                    p.setStock(p.getStock() - ci.getQuantity());
                }
                BigDecimal itemTotal = p.getPrice() != null ?
                        p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())) : BigDecimal.ZERO;
                total = total.add(itemTotal);
            }
        }

        order.setTotalAmount(total);
        order.setFinalAmount(total);

        if (orderRepository != null) {
            return orderRepository.save(order);
        } else {
            memoryOrders.add(order);
            return order;
        }
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        if (orderRepository != null) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

            // Nếu đơn hàng chuyển sang CANCELLED và đơn trước đó chưa bị hủy -> hoàn lại tồn kho
            if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    if (product != null && productRepository != null) {
                        product.setStock(product.getStock() + item.getQuantity());
                        productRepository.save(product);
                    }
                }
            }

            order.setStatus(newStatus);
            return orderRepository.save(order);
        } else {
            return memoryOrders.stream()
                    .filter(o -> o.getId().equals(orderId))
                    .findFirst()
                    .map(o -> {
                        o.setStatus(newStatus);
                        return o;
                    })
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));
        }
    }

    @Transactional
    public Order placeOrder(Long userId, OrderRequest request) {
        if (userRepository == null || cartService == null || orderRepository == null || productRepository == null) {
            throw new IllegalStateException("Hệ thống cơ sở dữ liệu chưa sẵn sàng");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng ID: " + userId));

        Cart cart = cartService.getCartByUserId(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống, không thể tạo đơn hàng");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderCode("ORD-" + System.currentTimeMillis());
        order.setCustomerName(request.getCustomerName() != null ? request.getCustomerName() : user.getFullName());
        order.setPhone(request.getPhone() != null ? request.getPhone() : user.getPhone());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(PaymentMethod.COD);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException("Sản phẩm '" + product.getName() + "' không đủ số lượng để đặt hàng");
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            orderItem.setSubtotal(subtotal);

            totalAmount = totalAmount.add(subtotal);
            order.getItems().add(orderItem);
        }

        order.setTotalAmount(totalAmount);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank() && couponRepository != null) {
            Coupon coupon = couponRepository.findByCodeIgnoreCase(request.getCouponCode().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại"));

            if (!coupon.isActive()) {
                throw new IllegalArgumentException("Mã giảm giá không còn hiệu lực");
            }
            if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Mã giảm giá đã hết hạn");
            }
            if (coupon.getQuantity() != null && coupon.getQuantity() <= 0) {
                throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng");
            }
            if (coupon.getMinOrderAmount() != null && totalAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
                throw new IllegalArgumentException("Đơn hàng cần đạt tối thiểu " + coupon.getMinOrderAmount() + "đ để áp dụng mã");
            }

            if (coupon.getDiscountType() == DiscountType.PERCENT) {
                discountAmount = totalAmount.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
                if (coupon.getMaxDiscountAmount() != null && discountAmount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                    discountAmount = coupon.getMaxDiscountAmount();
                }
            } else {
                discountAmount = coupon.getDiscountValue();
            }

            if (coupon.getQuantity() != null) {
                coupon.setQuantity(coupon.getQuantity() - 1);
                couponRepository.save(coupon);
            }
        }

        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(finalAmount);

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(userId);

        return savedOrder;
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository != null ? orderRepository.findByUserId(userId) : new ArrayList<>();
    }

    public Order getOrderById(Long id) {
        if (orderRepository == null) {
            return memoryOrders.stream().filter(o -> o.getId().equals(id)).findFirst().orElse(null);
        }
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + id));
    }
}