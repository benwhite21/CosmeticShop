package org.example.cosmeticshop.controller;

import org.example.cosmeticshop.entity.Product;
import org.example.cosmeticshop.repository.BrandRepository;
import org.example.cosmeticshop.repository.CategoryRepository;
import org.example.cosmeticshop.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public AdminProductController(ProductRepository productRepository,
                                  CategoryRepository categoryRepository,
                                  BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) Long brandId) {
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(product::setCategory);
        }
        if (brandId != null) {
            brandRepository.findById(brandId).ifPresent(product::setBrand);
        }
        return ResponseEntity.ok(productRepository.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @RequestBody Product req,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) Long brandId) {
        return productRepository.findById(id).map(prod -> {
            prod.setName(req.getName());
            prod.setPrice(req.getPrice());
            prod.setStock(req.getStock());
            prod.setImageUrl(req.getImageUrl());
            prod.setDescription(req.getDescription());

            if (categoryId != null) {
                categoryRepository.findById(categoryId).ifPresent(prod::setCategory);
            }
            if (brandId != null) {
                brandRepository.findById(brandId).ifPresent(prod::setBrand);
            }
            return ResponseEntity.ok(productRepository.save(prod));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}