package org.example.cosmeticshop.service;

import org.example.cosmeticshop.entity.Cart;
import org.example.cosmeticshop.entity.CartItem;
import org.example.cosmeticshop.entity.Product;
import org.example.cosmeticshop.entity.User;
import org.example.cosmeticshop.repository.CartRepository;
import org.example.cosmeticshop.repository.ProductRepository;
import org.example.cosmeticshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired(required = false)
    private CartRepository cartRepository;

    @Autowired(required = false)
    private ProductRepository productRepository;

    @Autowired(required = false)
    private UserRepository userRepository;

    private final List<CartItem> memoryItems = new ArrayList<>();

    public CartService() {}

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public void addToCart(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Sản phẩm không được để null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng thêm phải lớn hơn 0");
        }
        if (product.getStock() != null && product.getStock() < quantity) {
            throw new IllegalArgumentException("Sản phẩm tồn kho không đủ");
        }

        for (CartItem item : memoryItems) {
            if (item.getProduct() != null && item.getProduct().getId() != null &&
                    item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        CartItem newItem = new CartItem();
        newItem.setProduct(product);
        newItem.setQuantity(quantity);
        memoryItems.add(newItem);
    }

    public List<CartItem> getCartItems() {
        return memoryItems;
    }

    public Double getTotalAmount() {
        double total = 0.0;
        for (CartItem item : memoryItems) {
            if (item.getSubtotal() != null) {
                total += item.getSubtotal();
            }
        }
        return total;
    }

    @Transactional
    public Cart getCartByUserId(Long userId) {
        if (cartRepository == null || userRepository == null) {
            return new Cart();
        }
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + userId));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public Cart addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng thêm phải lớn hơn 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + productId));

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Sản phẩm tồn kho không đủ (chỉ còn " + product.getStock() + ")");
        }

        Cart cart = getCartByUserId(userId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (product.getStock() < newQuantity) {
                throw new IllegalArgumentException("Số lượng vượt quá tồn kho");
            }
            item.setQuantity(newQuantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateQuantity(Long userId, Long productId, int quantity) {
        Cart cart = getCartByUserId(userId);

        if (quantity <= 0) {
            cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + productId));

            if (product.getStock() < quantity) {
                throw new IllegalArgumentException("Tồn kho chỉ còn " + product.getStock());
            }

            for (CartItem item : cart.getItems()) {
                if (item.getProduct().getId().equals(productId)) {
                    item.setQuantity(quantity);
                    break;
                }
            }
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        if (cartRepository != null) {
            Cart cart = getCartByUserId(userId);
            cart.getItems().clear();
            cartRepository.save(cart);
        } else {
            memoryItems.clear();
        }
    }
}