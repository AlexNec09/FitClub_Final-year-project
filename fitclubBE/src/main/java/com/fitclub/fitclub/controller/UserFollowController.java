package com.fitclub.fitclub.controller;

import com.fitclub.fitclub.model.Entity.User;
import com.fitclub.fitclub.service.UserService;
import com.fitclub.fitclub.shared.CurrentUser;
import com.fitclub.fitclub.shared.GenericResponse;
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
        return new GenericResponse("Follow success");
    }

    @PutMapping("/users/{id:[0-9]+}/unfollow")
    @PreAuthorize("#id != principal.id")
    GenericResponse handleUnFollow(@PathVariable long id, @CurrentUser User currentUser) {
        userService.unfollow(id, currentUser);
        return new GenericResponse("Unfollow success");
    }
}
