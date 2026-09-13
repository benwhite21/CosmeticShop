package org.example.cosmeticshop.dto;

public class TopSellingProductDTO {
    private Long productId;
    private String productName;
    private Long totalQuantitySold;

    public TopSellingProductDTO() {}

    public TopSellingProductDTO(Long productId, String productName, Long totalQuantitySold) {
        this.productId = productId;
        this.productName = productName;
        this.totalQuantitySold = totalQuantitySold;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getTotalQuantitySold() {
        return totalQuantitySold;
    }

    public void setTotalQuantitySold(Long totalQuantitySold) {
        this.totalQuantitySold = totalQuantitySold;
    }
}