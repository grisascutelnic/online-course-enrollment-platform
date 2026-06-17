package com.internship.course_service.ai.controller;

import com.internship.course_service.ai.dto.AiChatRequest;
import com.internship.course_service.ai.dto.AiChatResponse;
import com.internship.course_service.ai.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public AiChatResponse chat(
            @RequestBody AiChatRequest request,
            Authentication authentication
    ) {
        String answer = aiChatService.chat(request.message(), authentication);
        return new AiChatResponse(answer);
    }
}