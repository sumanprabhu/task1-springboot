package com.task1.suman.ai.tools;

import com.task1.suman.ai.tools.ToolRecords.*;
import com.task1.suman.model.User;
import com.task1.suman.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component("searchByCityTool")
@Description("Finds all users who live in a specific city. The city name search is case-insensitive. For example, 'bangalore' will match 'Bangalore'.")
public class SearchByCityTool
        implements Function<SearchByCityRequest, UserListResponse> {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserListResponse apply(SearchByCityRequest request) {
        try {
            if (request.city() == null || request.city().isBlank()) {
                return new UserListResponse("FAILED", 0, List.of());
            }

            List<User> allUsers = userRepo.findAll();

            List<User> filtered = allUsers.stream()
                    .filter(u -> u.getAddress() != null
                            && u.getAddress().getCity() != null
                            && u.getAddress().getCity().toLowerCase()
                            .contains(request.city().toLowerCase()))
                    .toList();

            List<UserSummary> summaries = filtered.stream()
                    .map(u -> new UserSummary(
                            u.getName(),
                            u.getEmail(),
                            u.getRole() != null ? u.getRole().getRoleName() : "NONE",
                            u.getAddress().getCity()
                    ))
                    .toList();

            return new UserListResponse("SUCCESS", summaries.size(), summaries);

        } catch (Exception e) {
            return new UserListResponse("FAILED", 0, List.of());
        }
    }
}