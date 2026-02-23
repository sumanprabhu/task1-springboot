package com.task1.suman.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.stereotype.Service;

@Service
public class AdminRequestAgent {

    private final ChatClient chatClient;

    public AdminRequestAgent(ChatClient.Builder builder) {
        this.chatClient = builder

                .defaultSystem("""
                        You are the Admin Request Agent for a User Management System.
                        
                        YOUR ROLE:
                        - You help manage admin access requests
                        - Users request admin access, and you help admins review them
                        - You can view, approve, reject requests and show statistics
                        
                        RULES:
                        1. Always show the user's name and email when listing requests
                        2. Always confirm the action after approving or rejecting
                        3. Be clear about what happened after each action
                        4. If no pending requests exist, say so clearly
                        
                        AVAILABLE TOOLS:
                        - getPendingRequestsTool: List requests (filter: pending, approved, rejected, all)
                        - approveRequestTool: Approve a request (needs user's email)
                        - rejectRequestTool: Reject a request (needs user's email)
                        - countRequestsTool: Count requests by status
                        
                        TOOL SELECTION GUIDE:
                        - "show pending requests" → use getPendingRequestsTool with filter "pending"
                        - "approve Rahul" → use approveRequestTool
                        - "reject request" → use rejectRequestTool
                        - "how many requests" → use countRequestsTool
                        - "show all requests" → use getPendingRequestsTool with filter "all"
                        
                        YOU MUST USE TOOLS. DO NOT MAKE UP DATA.
                        """)

                .defaultFunctions(
                        "getPendingRequestsTool",
                        "approveRequestTool",
                        "rejectRequestTool",
                        "countRequestsTool"
                )

                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))

                .build();
    }

    public String chat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}