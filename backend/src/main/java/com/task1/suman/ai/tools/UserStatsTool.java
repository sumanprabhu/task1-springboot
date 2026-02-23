package com.task1.suman.ai.tools;

import com.task1.suman.ai.tools.ToolRecords.*;
import com.task1.suman.model.User;
import com.task1.suman.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("userStatsTool")
@Description("Provides statistics and summary about users in the system. Returns total users count, admin count, user count, number of users with and without address, and city-wise distribution.")
public class UserStatsTool
        implements Function<StatsRequest, StatsResponse> {

    @Autowired
    private UserRepo userRepo;

    @Override
    public StatsResponse apply(StatsRequest request) {
        try {
            List<User> allUsers = userRepo.findAll();

            long totalUsers = allUsers.size();

            long adminCount = allUsers.stream()
                    .filter(u -> u.getRole() != null
                            && u.getRole().getRoleName().equals("ADMIN"))
                    .count();

            long userCount = allUsers.stream()
                    .filter(u -> u.getRole() != null
                            && u.getRole().getRoleName().equals("USER"))
                    .count();

            long withAddress = allUsers.stream()
                    .filter(u -> u.getAddress() != null)
                    .count();

            long withoutAddress = totalUsers - withAddress;

            // City-wise count
            Map<String, Long> cityStats = allUsers.stream()
                    .filter(u -> u.getAddress() != null && u.getAddress().getCity() != null)
                    .collect(Collectors.groupingBy(
                            u -> u.getAddress().getCity(),
                            Collectors.counting()
                    ));

            String summary = String.format(
                    "Total Users: %d\n" +
                            "Admins: %d\n" +
                            "Regular Users: %d\n" +
                            "Users with address: %d\n" +
                            "Users without address: %d\n" +
                            "City-wise distribution: %s",
                    totalUsers, adminCount, userCount,
                    withAddress, withoutAddress,
                    cityStats.isEmpty() ? "No city data" : cityStats.toString()
            );

            return new StatsResponse("SUCCESS", summary);

        } catch (Exception e) {
            return new StatsResponse("FAILED", "Error: " + e.getMessage());
        }
    }
}