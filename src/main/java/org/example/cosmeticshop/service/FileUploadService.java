package org.example.cosmeticshop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Không thể tải lên file rỗng!");
        }

        // Kiểm tra định dạng file ảnh hợp lệ
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ chấp nhận các file định dạng hình ảnh (.jpg, .jpeg, .png, .webp)!");
        }

        try {
            Path targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(targetLocation)) {
                Files.createDirectories(targetLocation);
            }

            // Đặt tên file ngẫu nhiên để tránh trùng lặp
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            Path targetPath = targetLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về đường dẫn truy cập ảnh
            return "/uploads/" + newFileName;

        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu trữ file. Vui lòng thử lại!", e);
        }
    }
}