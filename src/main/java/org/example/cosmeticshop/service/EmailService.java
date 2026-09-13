package org.example.cosmeticshop.service;

import org.example.cosmeticshop.entity.Order;
import org.example.cosmeticshop.entity.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendOrderConfirmationEmail(Order order) {
        String recipientEmail = order.getUser() != null ? order.getUser().getEmail() : "customer@gmail.com";
        String subject = "CosmeticShop - Xác nhận đơn hàng #" + order.getId();

        StringBuilder content = new StringBuilder();
        content.append("Xin chào ").append(order.getCustomerName()).append(",\n\n");
        content.append("Đơn hàng #").append(order.getId()).append(" của bạn đã được tiếp nhận thành công!\n\n");
        content.append("Địa chỉ giao: ").append(order.getShippingAddress()).append("\n");
        content.append("Số điện thoại: ").append(order.getPhone()).append("\n");
        content.append("--------------------------------------------------\n");
        content.append("CHI TIẾT ĐƠN HÀNG:\n");

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                content.append("- ").append(item.getProductName())
                        .append(" x").append(item.getQuantity())
                        .append(" : ").append(item.getPrice()).append(" đ\n");
            }
        }
        content.append("--------------------------------------------------\n");
        content.append("Tổng thanh toán: ").append(order.getFinalAmount()).append(" đ\n\n");
        content.append("Cảm ơn bạn đã tin tưởng và ủng hộ CosmeticShop!");

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(recipientEmail);
                message.setSubject(subject);
                message.setText(content.toString());
                mailSender.send(message);
            } catch (Exception e) {
                System.err.println("Gửi mail thực tế thất bại (bỏ qua nếu chưa cấu hình mật khẩu SMTP): " + e.getMessage());
            }
        }

        // Luôn in bản xem trước email ra log console
        System.out.println("========== [XÁC NHẬN ĐƠN HÀNG - EMAIL GỬI ĐẾN " + recipientEmail + "] ==========");
        System.out.println(content);
        System.out.println("================================================================================");
    }
}