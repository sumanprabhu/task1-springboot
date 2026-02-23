package com.task1.suman.ai.tools;

import java.util.List;

public class ToolRecords {

    // ===== CREATE USER =====
    public record CreateUserRequest(
            String name,
            String email,
            String contactNum
    ) {}

    public record CreateUserResponse(
            String status,
            String message
    ) {}

    // ===== FIND USER =====
    public record FindUserRequest(
            String email
    ) {}

    public record FindUserResponse(
            String name,
            String email,
            String contactNum,
            String role,
            String city
    ) {}

    // ===== DELETE USER =====
    public record DeleteUserRequest(
            String email
    ) {}

    // ===== CHANGE ROLE =====
    public record ChangeRoleRequest(
            String email,
            String newRole
    ) {}

    // ===== LIST USERS =====
    public record ListUsersRequest(
            String filter
    ) {}

    // ===== SEARCH BY NAME =====
    public record SearchByNameRequest(
            String name
    ) {}

    // ===== SEARCH BY CITY =====
    public record SearchByCityRequest(
            String city
    ) {}

    // ===== ADDRESS FILTER =====
    public record AddressFilterRequest(
            String filter
    ) {}

    // ===== USER STATS =====
    public record StatsRequest(
            String type
    ) {}

    public record StatsResponse(
            String status,
            String summary
    ) {}

    // ===== COMMON RESPONSES =====
    public record SimpleResponse(
            String status,
            String message
    ) {}

    public record UserSummary(
            String name,
            String email,
            String role,
            String city
    ) {}

    public record UserListResponse(
            String status,
            int count,
            List<UserSummary> users
    ) {}

    // ===== ADMIN REQUEST TOOLS =====

    public record PendingRequestsRequest(
            String filter
            // "all", "pending", "approved", "rejected"
    ) {}

    public record AdminRequestSummary(
            String id,
            String userName,
            String userEmail,
            String reason,
            String status,
            String requestedAt
    ) {}

    public record AdminRequestListResponse(
            String status,
            int count,
            java.util.List<AdminRequestSummary> requests
    ) {}

    public record ApproveRejectRequest(
            String userEmail
    ) {}

    public record RequestCountResponse(
            String status,
            int pending,
            int approved,
            int rejected,
            int total
    ) {}
}