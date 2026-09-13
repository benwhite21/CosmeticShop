package org.example.cosmeticshop.controller;

import org.example.cosmeticshop.entity.Cart;
import org.example.cosmeticshop.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestParam Long userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Long userId,
                                       @RequestParam Long productId,
                                       @RequestParam(defaultValue = "1") int quantity) {
        try {
            Cart cart = cartService.addToCart(userId, productId, quantity);
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestParam Long userId,
                                            @RequestParam Long productId,
                                            @RequestParam int quantity) {
        try {
            Cart cart = cartService.updateQuantity(userId, productId, quantity);
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestParam Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok("Đã làm trống giỏ hàng");
    }
}