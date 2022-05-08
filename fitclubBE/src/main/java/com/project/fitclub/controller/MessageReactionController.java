package com.project.fitclub.controller;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.User;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.service.MessageReactionService;
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
public class MessageReactionController {

    @Autowired
    MessageReactionService messageReactionService;

    @Autowired
    UserRepository userRepository;

    @PutMapping("/messages/{id:[0-9]+}/like")
    @PreAuthorize("hasRole('USER')")
    GenericResponse handleLike(@PathVariable long id, @CurrentUser UserPrincipal userPrincipal) {
        User currentUser = userRepository.findByUsername(userPrincipal.getUsername());

        messageReactionService.like(id, currentUser);
        return new GenericResponse("You liked the post.");
    }

    @PutMapping("/messages/{id:[0-9]+}/dislike")
    @PreAuthorize("hasRole('USER')")
    GenericResponse handleDislike(@PathVariable long id, @CurrentUser UserPrincipal userPrincipal) {
        User currentUser = userRepository.findByUsername(userPrincipal.getUsername());

        messageReactionService.dislike(id, currentUser);
        return new GenericResponse("You disliked the post.");
    }

}