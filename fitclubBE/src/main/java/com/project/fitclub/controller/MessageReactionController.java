package com.project.fitclub.controller;

import com.project.fitclub.model.User;
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

    @PutMapping("/messages/{id:[0-9]+}/like")
    @PreAuthorize("hasRole('USER')")
    GenericResponse handleLike(@PathVariable long id, @CurrentUser User user) {
        messageReactionService.like(id, user);
        return new GenericResponse("You liked the post.");
    }

    @PutMapping("/messages/{id:[0-9]+}/dislike")
    @PreAuthorize("hasRole('USER')")
    GenericResponse handleDislike(@PathVariable long id, @CurrentUser User user) {
        messageReactionService.dislike(id, user);
        return new GenericResponse("You disliked the post.");
    }

}