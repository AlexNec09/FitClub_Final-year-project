package com.project.fitclub.controller;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.User;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.service.PostReactionService;
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
public class PostReactionController {

    @Autowired
    PostReactionService postReactionService;

    @Autowired
    UserRepository userRepository;

    @PutMapping("/posts/{id:[0-9]+}/like")
    GenericResponse handleLike(@PathVariable long id, @CurrentUser UserPrincipal userPrincipal) {
        User currentUser = userRepository.findByUsername(userPrincipal.getUsername());

        postReactionService.like(id, currentUser);
        return new GenericResponse("You liked the post.");
    }

    @PutMapping("/posts/{id:[0-9]+}/dislike")
    GenericResponse handleDislike(@PathVariable long id, @CurrentUser UserPrincipal userPrincipal) {
        User currentUser = userRepository.findByUsername(userPrincipal.getUsername());

        postReactionService.dislike(id, currentUser);
        return new GenericResponse("You disliked the post.");
    }

}