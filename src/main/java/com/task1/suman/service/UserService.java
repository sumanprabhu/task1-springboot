package com.task1.suman.service;

import com.task1.suman.model.Role;
import com.task1.suman.model.User;
import com.task1.suman.repo.RoleRepo;
import com.task1.suman.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    public Page<User> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return userRepo.findAll(pageable);
    }

    public User getUser(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with id: " + id
                        )
                );
    }

    public User addUser(User user) {

        if (user.getRole() != null) {
            Role role = roleRepo.findById(user.getRole().getId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Invalid role"
                            )
                    );

            user.setRole(role);
        }

        return userRepo.save(user);
    }

    public User updateUser(UUID id, User user) {

        User existingUser = userRepo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with id: " + id
                        )
                );

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setContactNum(user.getContactNum());

        // Update Address
        if (user.getAddress() != null) {
            existingUser.setAddress(user.getAddress());
        }

        // Update Role (only one role allowed now)
        if (user.getRole() != null) {
            Role role = roleRepo.findById(user.getRole().getId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Invalid role"
                            )
                    );

            existingUser.setRole(role);
        }

        return userRepo.save(existingUser);
    }

    public void deleteUser(UUID id) {
        userRepo.deleteById(id);
    }

    public List<User> getUserByName(String name) {
        return userRepo.findAllByNameContainingIgnoreCase(name);
    }
}

