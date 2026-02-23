package com.task1.suman.ai.tools;

import com.task1.suman.ai.tools.ToolRecords.*;
import com.task1.suman.model.User;
import com.task1.suman.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component("searchByNameTool")
@Description("Searches for users whose name contains the given text. For example, searching 'Rah' will find 'Rahul', 'Raheem', etc. The search is case-insensitive.")
public class SearchByNameTool
        implements Function<SearchByNameRequest, UserListResponse> {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserListResponse apply(SearchByNameRequest request) {
        try {
            if (request.name() == null || request.name().isBlank()) {
                return new UserListResponse("FAILED", 0, List.of());
            }

            List<User> users = userRepo.findAllByNameContainingIgnoreCase(request.name());

            List<UserSummary> summaries = users.stream()
                    .map(u -> new UserSummary(
                            u.getName(),
                            u.getEmail(),
                            u.getRole() != null ? u.getRole().getRoleName() : "NONE",
                            u.getAddress() != null ? u.getAddress().getCity() : "No address"
                    ))
                    .toList();

            return new UserListResponse("SUCCESS", summaries.size(), summaries);

        } catch (Exception e) {
            return new UserListResponse("FAILED", 0, List.of());
        }
    }
}