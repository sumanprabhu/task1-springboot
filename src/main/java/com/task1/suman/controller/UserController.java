package com.task1.suman.controller;

import com.task1.suman.model.User;
import com.task1.suman.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping()
    public ResponseEntity<List<User>> getUsers(){
        return new ResponseEntity<>(userService.getUsers(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id){
        return new ResponseEntity<>(userService.getUser(id),HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<User>> getUserByName(@PathVariable String name){
        return new ResponseEntity<>(userService.getUserByName(name),HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<User> addUser(@RequestBody User user){
        User newUser= userService.addUser(user);
        return new ResponseEntity<>(newUser,HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id,@RequestBody User user){
        return new ResponseEntity<>(userService.updateUser(id,user),HttpStatus.OK);
    }

    @PutMapping("/{id}/roles/{roleID}")
    public ResponseEntity<User> assignRole(@PathVariable UUID id,@PathVariable Long roleID){
        return ResponseEntity.ok(userService.assignRole(id,roleID));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID id){
        userService.deleteUser(id);
        return ResponseEntity.ok("Deleted user with the specified id");
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    public ResponseEntity<User> removeRole(@PathVariable UUID id, @PathVariable Long roleId){
        return ResponseEntity.ok(userService.removeRole(id,roleId));
    }

}
