package com.task1.suman.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final ChatClient chatClient;

    @Autowired
    private UserAdminAgent userAdminAgent;

    @Autowired
    private AdminRequestAgent adminRequestAgent;

    @Autowired
    private OrchestratorAgent orchestratorAgent;

    public AiController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    // Direct User Admin Agent
    @PostMapping("/agent")
    public Map<String, String> agentChat(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String message = body.get("message");
        String role = getRole(authentication);
        String response = userAdminAgent.chat(message, role);
        return Map.of("response", response);
    }

    // Direct Admin Request Agent
    @PostMapping("/agent/requests")
    public Map<String, String> requestAgentChat(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String role = getRole(authentication);
        if (!role.equals("ADMIN")) {
            return Map.of("response", "❌ Access denied! Only ADMINs can manage admin requests.");
        }
        String message = body.get("message");
        String response = adminRequestAgent.chat(message);
        return Map.of("response", response);
    }

    // ✅ NEW — Orchestrator (auto-routes to correct agent)
    @PostMapping("/agent/orchestrator")
    public Map<String, String> orchestratorChat(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String message = body.get("message");
        String role = getRole(authentication);
        String response = orchestratorAgent.chat(message, role);
        return Map.of("response", response);
    }

    private String getRole(Authentication authentication) {
        if (authentication == null) return "USER";
        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return isAdmin ? "ADMIN" : "USER";
    }
}