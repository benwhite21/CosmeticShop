package org.example.cosmeticshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    // Cho phép Jackson tuần tự hóa Product nhưng bỏ qua các quan hệ lồng nhau không cần thiết
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "images", "category", "brand"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;

    public CartItem() {}

    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public CartItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Các trường JSON tiện ích để frontend luôn đọc được kể cả khi cấu trúc phẳng
    @JsonProperty("productId")
    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

    @JsonProperty("productName")
    public String getProductName() {
        return product != null ? product.getName() : "Mỹ phẩm chính hãng";
    }

    @JsonProperty("price")
    public BigDecimal getPrice() {
        return product != null ? product.getPrice() : BigDecimal.ZERO;
    }

    @JsonProperty("imageUrl")
    public String getImageUrl() {
        return product != null ? product.getImageUrl() : "https://placehold.co/65x65?text=SP";
    }

    @JsonProperty("subtotal")
    public Double getSubtotal() {
        if (product == null || product.getPrice() == null || quantity == null) {
            return 0.0;
        }
        return product.getPrice().multiply(BigDecimal.valueOf(quantity)).doubleValue();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}