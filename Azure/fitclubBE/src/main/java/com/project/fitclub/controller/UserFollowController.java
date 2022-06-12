package com.project.fitclub.controller;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.User;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.service.UserService;
import com.project.fitclub.shared.CurrentUser;
import com.project.fitclub.shared.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@RestController
@RequestMapping("/api/1.0")
public class UserFollowController {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @PutMapping("/users/{id:[0-9]+}/follow")
    public ResponseEntity<?> handleFollow(@PathVariable long id, @CurrentUser UserPrincipal userPrincipal) {
        if (id != userPrincipal.getId()) {
            userService.follow(id, userPrincipal.getId());
            return ResponseEntity.ok(Collections.singletonMap("result", "You followed this user."));
        } else {
            return ResponseEntity.ok(Collections.singletonMap("result", "FAIL"));
        }
    }

    @PutMapping("/users/{id:[0-9]+}/unfollow")
    public ResponseEntity<?> handleUnFollow(@PathVariable long id, @CurrentUser UserPrincipal userPrincipal) {
        if (id != userPrincipal.getId()) {
            userService.unfollow(id, userPrincipal.getId());
            return ResponseEntity.ok(Collections.singletonMap("result", "You unfollowed this user."));
        } else {
            return ResponseEntity.ok(Collections.singletonMap("result", "FAIL"));
        }
    }
}