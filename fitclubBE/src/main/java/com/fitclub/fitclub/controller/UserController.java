package com.fitclub.fitclub.controller;

import com.fitclub.fitclub.model.Entity.User;
import com.fitclub.fitclub.shared.CurrentUser;
import com.fitclub.fitclub.model.UserUpdateVM;
import com.fitclub.fitclub.model.UserVM;
import com.fitclub.fitclub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import com.fitclub.fitclub.shared.GenericResponse;

import java.io.IOException;

@RestController
@RequestMapping("/api/1.0")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/users")
    GenericResponse createUser(@Valid @RequestBody User user) {
        userService.save(user);
        return new GenericResponse("User saved");
    }

    @GetMapping("/users")
    Page<UserVM> getUsers(@CurrentUser User loggedInUser, Pageable page) {
        return userService.getUsers(loggedInUser, page).map(UserVM::new);
    }

//    @GetMapping("/users/{username}")
//    UserVM getUserByName(@PathVariable String username) {
//        User user = userService.getByUsername(username);
//        return new UserVM(user);
//    }

    @GetMapping("/users/{username}")
    UserVM getUserByName(@PathVariable String username, @CurrentUser User currentUser) {
        User user = userService.getByUsername(username);
        return new UserVM(user, currentUser);
    }

    @PutMapping("/users/{id:[0-9]+}")
    @PreAuthorize("#id == principal.id")
        // check if id is the same as the logged in user ID using SpEL
    UserVM updateUser(@PathVariable long id, @Valid @RequestBody(required = false) UserUpdateVM userUpdate) throws IOException {
        User updated = userService.update(id, userUpdate);
        return new UserVM(updated);
    }
}
