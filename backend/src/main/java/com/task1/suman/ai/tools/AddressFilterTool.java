package com.task1.suman.ai.tools;

import com.task1.suman.ai.tools.ToolRecords.*;
import com.task1.suman.model.User;
import com.task1.suman.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component("addressFilterTool")
@Description("Filters users based on whether they have an address or not. Use filter 'with_address' to find users who have an address. Use filter 'without_address' to find users who do not have an address.")
public class AddressFilterTool
        implements Function<AddressFilterRequest, UserListResponse> {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserListResponse apply(AddressFilterRequest request) {
        try {
            List<User> allUsers = userRepo.findAll();
            List<User> filtered;

            String filter = request.filter() != null ?
                    request.filter().toLowerCase() : "without_address";

            if (filter.contains("with") && !filter.contains("without")) {
                filtered = allUsers.stream()
                        .filter(u -> u.getAddress() != null)
                        .toList();
            } else {
                filtered = allUsers.stream()
                        .filter(u -> u.getAddress() == null)
                        .toList();
            }

            List<UserSummary> summaries = filtered.stream()
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