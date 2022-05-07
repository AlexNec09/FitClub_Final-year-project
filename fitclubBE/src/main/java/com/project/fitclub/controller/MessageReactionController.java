package com.project.fitclub.controller;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.User;
import com.project.fitclub.service.MessageReactionService;
import com.project.fitclub.shared.CurrentUser;
import com.project.fitclub.shared.GenericResponse;
import com.project.fitclub.shared.response.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    GenericResponse handleLike(@PathVariable long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User userDB = userRepository.findById(userPrincipal.getId()).get();
        messageReactionService.like(id, userDB);
        return new GenericResponse("You liked the post.");
    }

    @PutMapping("/messages/{id:[0-9]+}/dislike")
    GenericResponse handleDislike(@PathVariable long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User userDB = userRepository.findById(userPrincipal.getId()).get();
        messageReactionService.dislike(id, userDB);
        return new GenericResponse("You disliked the post.");
    }

}