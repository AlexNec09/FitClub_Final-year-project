package com.project.fitclub.controller;

import com.project.fitclub.dao.UserRepository;
import com.project.fitclub.model.Message;
import com.project.fitclub.model.User;
import com.project.fitclub.model.vm.MessageVM;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.service.MessageService;
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
public class MessageController {

    @Autowired
    MessageService messageService;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/messages")
//    @PreAuthorize("hasRole('USER')")
    Page<?> getAllMessages(Pageable pageable, @CurrentUser UserPrincipal userPrincipal) {
        if (userPrincipal != null) {
            return messageService.getMessagesForUser(pageable, userPrincipal.getId()).map(MessageVM::new);
        }
        return messageService.getAllMessages(pageable).map(MessageVM::new);
    }

    @GetMapping("/users/{username}/messages")
//    @PreAuthorize("hasRole('USER')")
    Page<?> getMessagesOfUser(@CurrentUser UserPrincipal userPrincipal, @PathVariable String username, Pageable pageable) {
        return messageService.getMessagesOfUser(username, pageable).map(MessageVM::new);
    }

    @PostMapping("/messages")
//    @PreAuthorize("hasRole('USER')")
    MessageVM createMessage(@Valid @RequestBody Message message, @CurrentUser UserPrincipal userPrincipal) {
        User user = userRepository.findByUsername(userPrincipal.getUsername());
        return new MessageVM(messageService.save(user, message));
    }

    @DeleteMapping("/messages/{id:[0-9]+}")
    @PreAuthorize("@messageSecurityService.isAllowedToDelete(#userPrincipal, #id)")
    GenericResponse deleteMessage(@CurrentUser UserPrincipal userPrincipal, @PathVariable long id) {
        messageService.deleteMessage(id);
        return new GenericResponse("Post removed!");
    }

    @GetMapping({"/messages/{id:[0-9]+}", "/users/{username}/messages/{id:[0-9]+}"})
    ResponseEntity<?> getMessagesRelative(@CurrentUser UserPrincipal userPrincipal, @PathVariable long id,
                                          @PathVariable(required = false) String username,
                                          @RequestParam(name = "direction", defaultValue = "after") String direction,
                                          @RequestParam(name = "count", defaultValue = "false", required = false) boolean count,
                                          Pageable pageable) {
        if (userPrincipal == null) {
            return ResponseEntity.badRequest().body("Full authentication is required!");
        }
        User user = userRepository.findByUsername(userPrincipal.getUsername());
        if (!direction.equalsIgnoreCase("after")) {
            return ResponseEntity.ok(messageService.getMessagesBefore(id, username, user, pageable).map(MessageVM::new));
        }

        if (count) {
            long newMessagesCount = messageService.countMessagesAfter(id, username, user);
            return ResponseEntity.ok(Collections.singletonMap("count", newMessagesCount));
        }

        List<MessageVM> newMessages = messageService.getMessagesAfter(id, username, user, pageable).stream()
                .map(MessageVM::new).collect(Collectors.toList());
        return ResponseEntity.ok(newMessages);
    }
}