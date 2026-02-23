package com.task1.suman.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrchestratorAgent {

    @Autowired
    private UserAdminAgent userAdminAgent;

    @Autowired
    private AdminRequestAgent adminRequestAgent;

    private final ChatClient routerClient;

    public OrchestratorAgent(ChatClient.Builder builder) {
        this.routerClient = builder
                .defaultSystem("""
                        You are a Router. Your ONLY job is to classify user messages.
                        
                        Based on the user's message, respond with EXACTLY one word:
                        
                        Reply "USER_ADMIN" if the message is about:
                        - Creating, finding, deleting, listing users
                        - Searching users by name, city, email
                        - Changing user roles
                        - User statistics
                        - Anything related to user management
                        
                        Reply "ADMIN_REQUESTS" if the message is about:
                        - Admin access requests
                        - Approving or rejecting requests
                        - Pending requests
                        - Request counts or status
                        
                        Reply "GENERAL" if the message is:
                        - A greeting (hi, hello)
                        - Not related to either category
                        - Unclear
                        
                        RESPOND WITH ONLY ONE WORD: USER_ADMIN or ADMIN_REQUESTS or GENERAL
                        NO OTHER TEXT.
                        """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

    public String chat(String userMessage, String role) {
        // Step 1: Router decides which agent
        String route = routerClient.prompt()
                .user(userMessage)
                .call()
                .content()
                .trim()
                .toUpperCase();

        // Step 2: Route to correct agent
        if (route.contains("ADMIN_REQUESTS")) {
            if (!role.equals("ADMIN")) {
                return "❌ You need ADMIN access to manage admin requests. " +
                        "Request admin access from your Profile page.";
            }
            return adminRequestAgent.chat(userMessage);
        } else if (route.contains("USER_ADMIN")) {
            return userAdminAgent.chat(userMessage, role);
        } else {
            // GENERAL — handle greetings etc.
            return handleGeneral(userMessage, role);
        }
    }

    private String handleGeneral(String userMessage, String role) {
        String greeting = "👋 Hi! I'm your AI Assistant. I can help you with:\n\n";

        if (role.equals("ADMIN")) {
            greeting += "🛡️ **User Management:**\n" +
                    "• Create, find, delete users\n" +
                    "• Search by name, city\n" +
                    "• Change user roles\n" +
                    "• View user statistics\n\n" +
                    "📋 **Admin Requests:**\n" +
                    "• View pending requests\n" +
                    "• Approve or reject requests\n" +
                    "• Request statistics\n\n" +
                    "Just ask me anything!";
        } else {
            greeting += "🔍 **User Search:**\n" +
                    "• Find users by name, email, city\n" +
                    "• View user statistics\n" +
                    "• List all users\n\n" +
                    "⚠️ For create/delete/role changes, you need ADMIN access.\n" +
                    "Request it from your Profile page!\n\n" +
                    "Just ask me anything!";
        }

        return greeting;
    }
}