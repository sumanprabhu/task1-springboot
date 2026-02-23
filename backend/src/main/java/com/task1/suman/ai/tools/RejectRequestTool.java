package com.task1.suman.ai.tools;

import com.task1.suman.ai.tools.ToolRecords.*;
import com.task1.suman.model.AdminRequest;
import com.task1.suman.model.User;
import com.task1.suman.repo.AdminRequestRepo;
import com.task1.suman.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Component("rejectRequestTool")
@Description("Rejects an admin access request for a user. Provide the user's email address. The user's role will remain as USER.")
public class RejectRequestTool
        implements Function<ApproveRejectRequest, SimpleResponse> {

    @Autowired
    private AdminRequestRepo adminRequestRepo;

    @Autowired
    private UserRepo userRepo;

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

            // Find pending request
            List<AdminRequest> requests = adminRequestRepo.findByUserId(user.getId());
            AdminRequest pending = requests.stream()
                    .filter(r -> r.getStatus().equals("PENDING"))
                    .findFirst()
                    .orElse(null);

            if (pending == null) {
                return new SimpleResponse("FAILED",
                        "No pending admin request found for " + user.getName());
            }

            // Reject
            pending.setStatus("REJECTED");
            pending.setDecidedAt(LocalDateTime.now());
            pending.setDecidedBy("AI Agent");
            adminRequestRepo.save(pending);

            return new SimpleResponse("SUCCESS",
                    user.getName() + "'s admin request has been REJECTED.");

        } catch (Exception e) {
            return new SimpleResponse("FAILED", "Error: " + e.getMessage());
        }
    }
}