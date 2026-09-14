package org.example.cosmeticshop.controller;

import org.example.cosmeticshop.entity.Product;
import org.example.cosmeticshop.repository.BrandRepository;
import org.example.cosmeticshop.repository.CategoryRepository;
import org.example.cosmeticshop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public HomeController(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    @GetMapping({"/", "/home"})
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> productPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            productPage = productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        } else if (categoryId != null) {
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("keyword", keyword);

        return "index";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

        model.addAttribute("product", product);
        return "product-detail";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/cart")
    public String cartPage() {
        return "cart";
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam(required = false) String orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "payment-success";
    }
    @GetMapping("/orders")
    public String ordersPage() {
        return "orders";
    }
    @GetMapping("/admin/orders")
    public String adminOrdersPage() {
        return "admin-orders";
    }
    @GetMapping("/admin/products")
    public String adminProductsPage() {
        return "admin-products";
    }
    @GetMapping({"/admin", "/admin/dashboard"})
    public String adminDashboardPage() {
        return "admin-dashboard";
    }
}