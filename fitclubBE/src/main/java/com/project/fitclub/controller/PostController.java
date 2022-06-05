package com.project.fitclub.controller;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.Post;
import com.project.fitclub.model.User;
import com.project.fitclub.model.vm.PostVM;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.service.PostService;
import com.project.fitclub.shared.CurrentUser;
import com.project.fitclub.shared.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/1.0")
public class PostController {

    @Autowired
    PostService postService;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/posts")
    Page<?> getAllMessages(Pageable pageable, @CurrentUser UserPrincipal userPrincipal) {
        if (userPrincipal != null) {
            return postService.getPostsForUser(pageable, userPrincipal.getId()).map(PostVM::new);
        }
        return postService.getAllMessages(pageable).map(PostVM::new);
    }

    @GetMapping("/users/{username}/posts")
    Page<?> getPostsOfUser(@CurrentUser UserPrincipal userPrincipal, @PathVariable String username, Pageable pageable) {
        return postService.getPostsOfUser(username, pageable).map(PostVM::new);
    }

    @PostMapping("/posts")
    PostVM createMessage(@Valid @RequestBody Post post, @CurrentUser UserPrincipal userPrincipal) {
        User user = userRepository.findByUsername(userPrincipal.getUsername());
        return new PostVM(postService.save(user, post));
    }

    @DeleteMapping("/posts/{id:[0-9]+}")
    @PreAuthorize("@postSecurityService.isAllowedToDelete(#userPrincipal, #id)")
    GenericResponse deletePost(@CurrentUser UserPrincipal userPrincipal, @PathVariable long id) {
        postService.deletePost(id);
        return new GenericResponse("Post removed!");
    }

    @GetMapping({"/posts/{id:[0-9]+}", "/users/{username}/posts/{id:[0-9]+}"})
    ResponseEntity<?> getPostsRelative(@CurrentUser UserPrincipal userPrincipal, @PathVariable long id,
                                       @PathVariable(required = false) String username,
                                       @RequestParam(name = "direction", defaultValue = "after") String direction,
                                       @RequestParam(name = "count", defaultValue = "false", required = false) boolean count,
                                       Pageable pageable) {
        if (userPrincipal == null) {
            return ResponseEntity.badRequest().body("Full authentication is required!");
        }
        User user = userRepository.findByUsername(userPrincipal.getUsername());
        if (!direction.equalsIgnoreCase("after")) {
            return ResponseEntity.ok(postService.getPostsBefore(id, username, user, pageable).map(PostVM::new));
        }

        if (count) {
            long newMessagesCount = postService.countPostsAfter(id, username, user);
            return ResponseEntity.ok(Collections.singletonMap("count", newMessagesCount));
        }

        List<PostVM> newMessages = postService.getPostsAfter(id, username, user, pageable).stream()
                .map(PostVM::new).collect(Collectors.toList());
        return ResponseEntity.ok(newMessages);
    }
}