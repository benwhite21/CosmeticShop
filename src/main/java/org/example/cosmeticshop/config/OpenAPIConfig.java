package org.example.cosmeticshop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI cosmeticShopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cosmetic Shop REST API")
                        .description("Hệ thống API quản lý bán mỹ phẩm trực tuyến: Người dùng, Sản phẩm, Giỏ hàng, Đơn hàng, Mã giảm giá.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cosmetic Support Team")
                                .email("support@cosmetic.com")));
    }
}