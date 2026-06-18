package com.internship.course_service.ai.service;

import com.internship.course_service.ai.tools.CourseTools;
import com.internship.course_service.ai.tools.StudentTools;
import com.internship.course_service.ai.tools.TeacherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            CourseTools courseTools,
            StudentTools studentTools,
            TeacherTools teacherTools
    ) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(
                        courseTools,
                        studentTools,
                        teacherTools
                )
                .build();
    }

    public String chat(String message, Authentication authentication) {
        String conversationId = authentication.getName();

        return chatClient
                .prompt()
                .system("""
                        You are an AI assistant for an Online Course Enrollment Platform.

                        You must only help with this platform:
                        - courses
                        - course explanations
                        - course comparison
                        - enrollment requests
                        - enrollment statuses
                        - teacher enrollment management

                        If the user asks what you can do, answer only with platform-related features.

                        Do not describe yourself as a general-purpose assistant.
                        Do not say that you can help with writing, history, science, creative work, or general research.

                        Use platform tools whenever the user asks about real courses or enrollment data.
                        Do not invent course data, enrollment data, users, statuses, or statistics.

                        Answer clearly, professionally, and briefly.
                        Use markdown formatting only when it improves readability.
                        Use bold text sparingly and avoid excessive emojis.
                        Prefer plain text and concise bullet points over decorative formatting.
                        """)
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}