package com.fitclub.fitclub.controller;

import com.fitclub.fitclub.model.Entity.User;
import com.fitclub.fitclub.service.MessageReactionService;
import com.fitclub.fitclub.shared.CurrentUser;
import com.fitclub.fitclub.shared.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
    GenericResponse handleLike(@PathVariable long id, @CurrentUser User user) {
        messageReactionService.like(id, user);
        return new GenericResponse("You liked the post.");
    }

    @PutMapping("/messages/{id:[0-9]+}/dislike")
    GenericResponse handleDisike(@PathVariable long id, @CurrentUser User user) {
        messageReactionService.dislike(id, user);
        return new GenericResponse("You disliked the post.");
    }

}
