package com.task1.suman.ai.tools;

import com.task1.suman.ai.tools.ToolRecords.*;
import com.task1.suman.model.AdminRequest;
import com.task1.suman.repo.AdminRequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component("getPendingRequestsTool")
@Description("Gets admin access requests. Can filter by status: 'pending' for waiting requests, 'approved' for approved ones, 'rejected' for rejected ones, or 'all' for everything.")
public class GetPendingRequestsTool
        implements Function<PendingRequestsRequest, AdminRequestListResponse> {

    @Autowired
    private AdminRequestRepo adminRequestRepo;

    @Override
    public AdminRequestListResponse apply(PendingRequestsRequest request) {
        try {
            List<AdminRequest> requests;
            String filter = request.filter() != null ?
                    request.filter().toUpperCase() : "PENDING";

            if (filter.equals("ALL")) {
                requests = adminRequestRepo.findAll();
            } else {
                requests = adminRequestRepo.findByStatus(filter);
            }

            List<AdminRequestSummary> summaries = requests.stream()
                    .map(r -> new AdminRequestSummary(
                            r.getId().toString(),
                            r.getUser() != null ? r.getUser().getName() : "Unknown",
                            r.getUser() != null ? r.getUser().getEmail() : "Unknown",
                            r.getReason(),
                            r.getStatus(),
                            r.getRequestedAt() != null ?
                                    r.getRequestedAt().toString() : "Unknown"
                    ))
                    .toList();

            return new AdminRequestListResponse(
                    "SUCCESS", summaries.size(), summaries
            );

        } catch (Exception e) {
            return new AdminRequestListResponse(
                    "FAILED", 0, List.of()
            );
        }
    }
}