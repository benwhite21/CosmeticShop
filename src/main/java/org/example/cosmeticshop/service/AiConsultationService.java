package org.example.cosmeticshop.service;

import org.example.cosmeticshop.entity.Product;
import org.example.cosmeticshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiConsultationService {

    private final ProductRepository productRepository;
    private final RestClient restClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    private List<String> cachedValidModels = new ArrayList<>();
    private String lastWorkingModel = null;
    private String cachedCatalog = "";
    private long lastCacheTime = 0;
    private static final long CACHE_TTL_MS = 5 * 60 * 1000;

    public AiConsultationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(8));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @SuppressWarnings("rawtypes")
    private List<String> getAvailableModels() {
        if (!cachedValidModels.isEmpty()) {
            return cachedValidModels;
        }
        try {
            Map res = restClient.get()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey.trim())
                    .retrieve()
                    .body(Map.class);

            if (res != null && res.containsKey("models")) {
                List models = (List) res.get("models");
                List<String> flashModels = new ArrayList<>();
                List<String> otherModels = new ArrayList<>();

                for (Object item : models) {
                    Map m = (Map) item;
                    String name = String.valueOf(m.get("name")).replace("models/", "");
                    List methods = (List) m.get("supportedGenerationMethods");

                    // Bỏ qua các model chuyên biệt (ảnh, âm thanh, embedding...) để tránh lỗi 400
                    if (name.contains("image") || name.contains("tts") || name.contains("audio") || name.contains("embedding")) {
                        continue;
                    }

                    if (methods != null && methods.contains("generateContent")) {
                        if (name.contains("flash")) {
                            flashModels.add(name);
                        } else if (name.contains("gemma") || name.contains("pro")) {
                            otherModels.add(name);
                        }
                    }
                }
                cachedValidModels.addAll(flashModels);
                cachedValidModels.addAll(otherModels);
                System.out.println("Danh sách model khả dụng trên Key: " + cachedValidModels);
            }
        } catch (Exception e) {
            System.out.println("Không lấy được danh sách model: " + e.getMessage());
        }

        if (cachedValidModels.isEmpty()) {
            cachedValidModels = List.of("gemini-3.8-flash", "gemma-3-27b-it");
        }
        return cachedValidModels;
    }

    private String getProductCatalog() {
        long now = System.currentTimeMillis();
        if (!cachedCatalog.isEmpty() && (now - lastCacheTime) < CACHE_TTL_MS) {
            return cachedCatalog;
        }
        List<Product> products = productRepository.findAll();
        cachedCatalog = products.stream()
                .map(p -> {
                    String desc = p.getDescription() != null ? p.getDescription() : "";
                    if (desc.length() > 100) {
                        desc = desc.substring(0, 100) + "...";
                    }
                    return String.format("- %s | Giá: %s VNĐ | %s", p.getName(), p.getPrice(), desc);
                })
                .collect(Collectors.joining("\n"));
        lastCacheTime = now;
        return cachedCatalog;
    }

    @SuppressWarnings("rawtypes")
    public String consultCustomer(String userMessage, String skinType) {
        try {
            String productCatalog = getProductCatalog();
            String prompt = """
                    Bạn là chuyên viên tư vấn của CosmeticShop. Trả lời ngắn gọn (tối đa 3-4 câu), thân thiện, gợi ý đúng sản phẩm có trong danh sách sau:
                    %s
                    
                    Khách hàng (Loại da: %s): %s
                    """.formatted(
                    productCatalog.isEmpty() ? "Chưa có dữ liệu sản phẩm." : productCatalog,
                    (skinType != null && !skinType.isBlank()) ? skinType : "Chưa rõ",
                    userMessage
            );

            // Đưa model vừa chạy thành công lên vị trí đầu tiên để phản hồi tức thì
            List<String> modelsToTry = new ArrayList<>(getAvailableModels());
            if (lastWorkingModel != null && modelsToTry.remove(lastWorkingModel)) {
                modelsToTry.add(0, lastWorkingModel);
            }

            for (String model : modelsToTry) {
                try {
                    Map<String, Object> generationConfig = new HashMap<>();
                    generationConfig.put("maxOutputTokens", 400);
                    generationConfig.put("temperature", 0.7);
                    if (model.startsWith("gemini-3") || model.startsWith("gemini-2.5")) {
                        generationConfig.put("thinkingConfig", Map.of("thinkingBudget", 0));
                    }

                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
                    if (!model.startsWith("gemma")) {
                        requestBody.put("generationConfig", generationConfig);
                    }

                    String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                            + model + ":generateContent?key=" + apiKey.trim();

                    Map response = restClient.post()
                            .uri(endpoint)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(requestBody)
                            .retrieve()
                            .body(Map.class);

                    if (response != null && response.containsKey("candidates")) {
                        List candidates = (List) response.get("candidates");
                        Map firstCandidate = (Map) candidates.get(0);
                        Map content = (Map) firstCandidate.get("content");
                        List parts = (List) content.get("parts");
                        Map firstPart = (Map) parts.get(0);

                        lastWorkingModel = model;
                        System.out.println("Tư vấn thành công bằng model: " + model);
                        return String.valueOf(firstPart.get("text"));
                    }
                } catch (RestClientResponseException e) {
                    System.out.println("Model " + model + " lỗi (" + e.getStatusCode().value() + "), thử model kế tiếp...");
                } catch (Exception e) {
                    System.out.println("Model " + model + " timeout/lỗi kết nối, chuyển model kế tiếp...");
                }
            }

            return fallbackLocalRecommendation(userMessage, skinType);

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    private String fallbackLocalRecommendation(String userMessage, String skinType) {
        String keyword = userMessage != null ? userMessage.toLowerCase() : "";
        List<Product> matched = productRepository.findAll().stream()
                .filter(p -> p.getName() != null && (
                        keyword.contains(p.getName().toLowerCase()) ||
                                (p.getDescription() != null && p.getDescription().toLowerCase().contains(keyword))
                ))
                .limit(3)
                .toList();

        if (!matched.isEmpty()) {
            String items = matched.stream()
                    .map(p -> p.getName() + " (" + p.getPrice() + " VNĐ)")
                    .collect(Collectors.joining(", "));
            return "Chào bạn, với tình trạng " + (skinType != null ? skinType : "da của bạn")
                    + ", CosmeticShop gợi ý bạn tham khảo các sản phẩm hiện có: " + items + ".";
        }
        return "Hiện máy chủ AI đang quá tải tạm thời. Bạn có thể tham khảo các sản phẩm nổi bật ngay tại trang chủ CosmeticShop hoặc thử nhắn lại sau vài giây nhé!";
    }
}