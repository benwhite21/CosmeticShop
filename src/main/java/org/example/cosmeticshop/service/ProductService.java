package org.example.cosmeticshop.service;

import org.example.cosmeticshop.entity.Category;
import org.example.cosmeticshop.entity.Product;
import org.example.cosmeticshop.entity.ProductStatus;
import org.example.cosmeticshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired(required = false)
    private ProductRepository productRepository;

    private final List<Product> memoryProducts = new ArrayList<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public ProductService() {}

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ================== CÁC PHƯƠNG THỨC CHO UNIT TEST CŨ ==================
    public Product addProduct(String name, String slug, String description, double price, int stock, String categoryName) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn hoặc bằng 0");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        }

        Product product = new Product();
        product.setId(idGen.getAndIncrement());
        product.setName(name);
        product.setSlug(slug);
        product.setDescription(description);
        product.setPrice(BigDecimal.valueOf(price));
        product.setStock(stock);
        product.setStatus(ProductStatus.ACTIVE);

        if (categoryName != null) {
            Category cat = new Category();
            cat.setName(categoryName);
            product.setCategory(cat);
        }

        if (productRepository != null) {
            return productRepository.save(product);
        } else {
            memoryProducts.add(product);
            return product;
        }
    }

    // Overload searchAndFilter cũ (4 tham số) cho Test
    public List<Product> searchAndFilter(String keyword, Long categoryId, Double minPrice, Double maxPrice) {
        if (productRepository == null) {
            return memoryProducts.stream()
                    .filter(p -> keyword == null || p.getName().toLowerCase().contains(keyword.toLowerCase()))
                    .filter(p -> categoryId == null || (p.getCategory() != null && p.getCategory().getId() != null && p.getCategory().getId().equals(categoryId)))
                    .filter(p -> minPrice == null || (p.getPrice() != null && p.getPrice().doubleValue() >= minPrice))
                    .filter(p -> maxPrice == null || (p.getPrice() != null && p.getPrice().doubleValue() <= maxPrice))
                    .collect(Collectors.toList());
        }

        BigDecimal min = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;
        Page<Product> page = searchAndFilter(keyword, categoryId, null, min, max, 0, 100, "id", "asc");
        return page.getContent();
    }

    // ================== CÁC PHƯƠNG THỨC LÀM VIỆC VỚI DATABASE (JPA) ==================
    public List<Product> getAllProducts() {
        if (productRepository != null) {
            return productRepository.findAll();
        }
        return memoryProducts;
    }

    public Product getProductById(Long id) {
        if (productRepository != null) {
            return productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + id));
        }
        return memoryProducts.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + id));
    }

    // Phân trang & Lọc nâng cao mới (9 tham số)
    public Page<Product> searchAndFilter(String keyword, Long categoryId, Long brandId,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         int page, int size, String sortBy, String direction) {
        if (productRepository == null) {
            throw new IllegalStateException("ProductRepository chưa được khởi tạo");
        }

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return productRepository.filterProducts(
                (keyword != null && !keyword.isBlank()) ? keyword.trim() : null,
                categoryId,
                brandId,
                minPrice,
                maxPrice,
                ProductStatus.ACTIVE,
                pageable
        );
    }
}