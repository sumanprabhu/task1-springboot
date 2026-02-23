package com.task1.suman.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.stereotype.Service;

@Service
public class UserAdminAgent {

    private final ChatClient.Builder clientBuilder;

    public UserAdminAgent(ChatClient.Builder builder) {
        this.clientBuilder = builder;
    }

    public String chat(String userMessage, String role) {
        ChatClient chatClient;

        if (role.equals("ADMIN")) {
            chatClient = clientBuilder
                    .defaultSystem("""
                            You are the User Admin Agent. Talking to an ADMIN with FULL access.
                            
                            RULES:
                            1. Confirm before deleting
                            2. Creating user needs: name, email, contactNum
                            3. Ask for missing info
                            4. Valid roles: ADMIN, USER
                            5. Be polite, summarize actions
                            
                            TOOLS:
                            - createUserTool: Create user
                            - findUserTool: Find by email
                            - deleteUserTool: Delete by email
                            - listUsersTool: List users (filter: all/admins/users)
                            - changeRoleTool: Change role
                            - searchByNameTool: Search by name
                            - searchByCityTool: Search by city
                            - userStatsTool: User statistics
                            - addressFilterTool: Filter by address
                            
                            USE TOOLS. DO NOT MAKE UP DATA.
                            """)
                    .defaultFunctions(
                            "createUserTool", "findUserTool", "deleteUserTool",
                            "listUsersTool", "changeRoleTool", "searchByNameTool",
                            "searchByCityTool", "userStatsTool", "addressFilterTool"
                    )
                    .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                    .build();
        } else {
            chatClient = clientBuilder
                    .defaultSystem("""
                            You are the User Admin Agent. Talking to a USER with LIMITED access.
                            
                            RULES:
                            1. You can ONLY search and view users
                            2. You CANNOT create, delete, or change roles
                            3. If asked to create/delete/change role, say:
                               "Sorry, you need ADMIN access. Request it from your Profile page."
                            
                            TOOLS (READ ONLY):
                            - findUserTool: Find by email
                            - listUsersTool: List users
                            - searchByNameTool: Search by name
                            - searchByCityTool: Search by city
                            - userStatsTool: Statistics
                            - addressFilterTool: Filter by address
                            
                            USE TOOLS. DO NOT MAKE UP DATA.
                            """)
                    .defaultFunctions(
                            "findUserTool", "listUsersTool", "searchByNameTool",
                            "searchByCityTool", "userStatsTool", "addressFilterTool"
                    )
                    .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                    .build();
        }

        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}