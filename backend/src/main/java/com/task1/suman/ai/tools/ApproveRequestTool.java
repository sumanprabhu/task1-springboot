package com.task1.suman.ai.tools;

import com.task1.suman.ai.tools.ToolRecords.*;
import com.task1.suman.model.AdminRequest;
import com.task1.suman.model.Role;
import com.task1.suman.model.User;
import com.task1.suman.repo.AdminRequestRepo;
import com.task1.suman.repo.RoleRepo;
import com.task1.suman.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Component("approveRequestTool")
@Description("Approves an admin access request for a user. Provide the user's email address. This will change the user's role from USER to ADMIN.")
public class ApproveRequestTool
        implements Function<ApproveRejectRequest, SimpleResponse> {

    @Autowired
    private AdminRequestRepo adminRequestRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Override
    public SimpleResponse apply(ApproveRejectRequest request) {
        try {
            if (request.userEmail() == null || request.userEmail().isBlank()) {
                return new SimpleResponse("FAILED",
                        "Please provide the user's email address.");
            }

            // Find user
            User user = userRepo.findByEmail(request.userEmail()).orElse(null);
            if (user == null) {
                return new SimpleResponse("FAILED",
                        "User with email " + request.userEmail() + " not found!");
            }

            // Find pending request for this user
            List<AdminRequest> requests = adminRequestRepo.findByUserId(user.getId());
            AdminRequest pending = requests.stream()
                    .filter(r -> r.getStatus().equals("PENDING"))
                    .findFirst()
                    .orElse(null);

            if (pending == null) {
                return new SimpleResponse("FAILED",
                        "No pending admin request found for " + user.getName());
            }

            // Change role to ADMIN
            Role adminRole = roleRepo.findByRoleName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            user.setRole(adminRole);
            userRepo.save(user);

            // Update request status
            pending.setStatus("APPROVED");
            pending.setDecidedAt(LocalDateTime.now());
            pending.setDecidedBy("AI Agent");
            adminRequestRepo.save(pending);

            return new SimpleResponse("SUCCESS",
                    user.getName() + "'s admin request has been APPROVED. They are now an ADMIN!");

        } catch (Exception e) {
            return new SimpleResponse("FAILED", "Error: " + e.getMessage());
        }
    }
}