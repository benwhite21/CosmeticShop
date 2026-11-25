package org.example.cosmeticshop.controller;

import org.example.cosmeticshop.dto.ChatRequest;
import org.example.cosmeticshop.dto.ChatResponse;
import org.example.cosmeticshop.service.AiConsultationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiConsultationService aiConsultationService;

    public AiChatController(AiConsultationService aiConsultationService) {
        this.aiConsultationService = aiConsultationService;
    }

    @PostMapping(value = "/consult", produces = "application/json;charset=UTF-8")
    public ResponseEntity<ChatResponse> consult(@RequestBody ChatRequest request) {
        String reply = aiConsultationService.consultCustomer(request.message(), request.skinType());
        return ResponseEntity.ok(new ChatResponse(reply));
    }

}