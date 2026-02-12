package com.task1.suman.service;

import com.task1.suman.model.Address;
import com.task1.suman.model.Role;
import com.task1.suman.model.User;
import com.task1.suman.repo.RoleRepo;
import com.task1.suman.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

    public List<User> getUsers() {
        return userRepo.findAll();
    }

    public User getUser(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(
                        ()->new ResponseStatusException(HttpStatus.NOT_FOUND,"user not found with the id :")
                );
    }

    public User addUser(User user) {
        return userRepo.save(user);
    }

    public User updateUser(UUID id,User user) {
        User existingUser = userRepo.findById(id)
                .orElseThrow(
                    ()->new ResponseStatusException(HttpStatus.NOT_FOUND,"user not found with the id :" +id)
                );
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setContactNum(user.getContactNum());

        if(user.getAddress()!=null){
            Address existingAddress = existingUser.getAddress();
            if(existingAddress==null)
                existingAddress=new Address();
            existingAddress.setStreet(user.getAddress().getStreet());
            existingAddress.setCity(user.getAddress().getCity());
            existingAddress.setState(user.getAddress().getState());
            existingAddress.setZipCode(user.getAddress().getZipCode());

            existingUser.setAddress(existingAddress);
        }

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {

            List<Long> roleIds = user.getRoles()
                    .stream()
                    .map(Role::getId)
                    .toList();

            List<Role> rolesFromDb = roleRepo.findAllById(roleIds);

            if (rolesFromDb.size() != roleIds.size()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "One or more roles are invalid"
                );
            }

            existingUser.setRoles(rolesFromDb);
        }

        return userRepo.save(existingUser);
    }

    public void deleteUser(UUID id) {
        userRepo.deleteById(id);
    }

    public List<User> getUserByName(String name) {
        return userRepo.findAllByNameContainingIgnoreCase(name);
    }

    public User assignRole(UUID id, Long roleID) {
        User user = userRepo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                );
        Role role = roleRepo.findById(roleID)
                .orElseThrow(()->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,"Role not found")
                );
        if(user.getRoles().contains(role))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User already has this role");
        user.getRoles().add(role);
        return userRepo.save(user);
    }

    public User removeRole(UUID id, Long roleId) {
        User user = userRepo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        Role role = roleRepo.findById(roleId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Role not found"
                        )
                );

        if (!user.getRoles().contains(role)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User does not have this role"
            );
        }
        user.getRoles().remove(role);
        return userRepo.save(user);
    }

}
