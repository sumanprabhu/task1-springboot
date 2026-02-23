package com.task1.suman.ai.tools;

import com.task1.suman.ai.tools.ToolRecords.*;
import com.task1.suman.model.AdminRequest;
import com.task1.suman.repo.AdminRequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component("countRequestsTool")
@Description("Counts admin access requests by status. Returns the number of pending, approved, rejected, and total requests.")
public class CountRequestsTool
        implements Function<PendingRequestsRequest, RequestCountResponse> {

    @Autowired
    private AdminRequestRepo adminRequestRepo;

    @Override
    public RequestCountResponse apply(PendingRequestsRequest request) {
        try {
            List<AdminRequest> all = adminRequestRepo.findAll();

            int pending = (int) all.stream()
                    .filter(r -> r.getStatus().equals("PENDING"))
                    .count();

            int approved = (int) all.stream()
                    .filter(r -> r.getStatus().equals("APPROVED"))
                    .count();

            int rejected = (int) all.stream()
                    .filter(r -> r.getStatus().equals("REJECTED"))
                    .count();

            return new RequestCountResponse(
                    "SUCCESS", pending, approved, rejected, all.size()
            );

        } catch (Exception e) {
            return new RequestCountResponse("FAILED", 0, 0, 0, 0);
        }
    }
}