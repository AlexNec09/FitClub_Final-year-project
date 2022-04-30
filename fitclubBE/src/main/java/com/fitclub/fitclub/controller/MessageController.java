package com.fitclub.fitclub.controller;

import com.fitclub.fitclub.service.MessageService;
import com.fitclub.fitclub.model.MessageVM;
import com.fitclub.fitclub.model.Entity.Message;
import com.fitclub.fitclub.shared.CurrentUser;
import com.fitclub.fitclub.shared.GenericResponse;
import com.fitclub.fitclub.model.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import javax.validation.Valid;

import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/1.0")
public class MessageController {

    @Autowired
    MessageService messageService;

    @PostMapping("/messages")
    MessageVM createMessage(@Valid @RequestBody Message message, @CurrentUser User user) {
        return new MessageVM(messageService.save(user, message));
    }

    @GetMapping("/messages")
    Page<MessageVM> getAllMessages(Pageable pageable) {
        return messageService.getAllMessages(pageable).map(MessageVM::new);
    }

    @GetMapping("/users/{username}/messages")
    Page<MessageVM> getMessagesOfUser(@PathVariable String username, Pageable pageable) {
        return messageService.getMessagesOfUser(username, pageable).map(MessageVM::new);
    }

    @GetMapping({"/messages/{id:[0-9]+}", "/users/{username}/messages/{id:[0-9]+}"})
    ResponseEntity<?> getMessagesRelative(@PathVariable long id,
                                          @PathVariable(required = false) String username,
                                          Pageable pageable,
                                          @RequestParam(name = "direction", defaultValue = "after") String direction,
                                          @RequestParam(name = "count", defaultValue = "false", required = false) boolean count) {
        if (!direction.equalsIgnoreCase("after")) {
            return ResponseEntity.ok(messageService.getOldMessages(id, username, pageable).map(MessageVM::new));
        }

        if (count) {
            long newMessagesCount = messageService.getNewMessagesCount(id, username);
            return ResponseEntity.ok(Collections.singletonMap("count", newMessagesCount));
        }

        List<MessageVM> newMessages = messageService.getNewMessages(id, username, pageable).stream()
                .map(MessageVM::new).collect(Collectors.toList());
        return ResponseEntity.ok(newMessages);
    }

    @DeleteMapping("/messages/{id:[0-9]+}")
    @PreAuthorize("@messageSecurityService.isAllowedToDelete(#id, principal)")
    GenericResponse deleteMessage(@PathVariable long id) {
        messageService.deleteMessage(id);
        return new GenericResponse("Message is removed!");
    }
}
