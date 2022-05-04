package com.project.fitclub.controller;

import com.project.fitclub.model.User;
import com.project.fitclub.service.UserService;
import com.project.fitclub.shared.CurrentUser;
import com.project.fitclub.shared.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/1.0")
public class UserFollowController {

    @Autowired
    UserService userService;

    @PutMapping("/users/{id:[0-9]+}/follow")
    @PreAuthorize("#id != principal.id")
    GenericResponse handleFollow(@PathVariable long id, @CurrentUser User currentUser) {
        userService.follow(id, currentUser);
        return new GenericResponse("You followed this user.");
    }

    @PutMapping("/users/{id:[0-9]+}/unfollow")
    @PreAuthorize("#id != principal.id")
    GenericResponse handleUnFollow(@PathVariable long id, @CurrentUser User currentUser) {
        userService.unfollow(id, currentUser);
        return new GenericResponse("You unfollowed this user.");
    }

}