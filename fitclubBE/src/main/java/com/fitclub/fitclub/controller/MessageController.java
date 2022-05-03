package com.fitclub.fitclub.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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

//    @GetMapping("/messages")
//    Page<MessageVM> getAllMessages(Pageable pageable) {
//        return messageService.getAllMessages(pageable).map(MessageVM::new);
//    }

    @GetMapping("/messages")
    Page<?> getAllMessages(Pageable pageable, @CurrentUser User user) {
        if (user != null)
            return messageService.getMessagesForUser(pageable, user.getUsername()).map(MessageVM::new);
        return messageService.getAllMessages(pageable).map(MessageVM::new);
    }

    @GetMapping("/users/{username}/messages")
    Page<MessageVM> getMessagesOfUser(@PathVariable String username, Pageable pageable) {
        return messageService.getMessagesOfUser(username, pageable).map(MessageVM::new);
    }

//    @GetMapping({"/messages/{id:[0-9]+}", "/users/{username}/messages/{id:[0-9]+}"})
//    ResponseEntity<?> getMessagesRelative(@PathVariable long id,
//                                          @PathVariable(required = false) String username,
//                                          Pageable pageable,
//                                          @RequestParam(name = "direction", defaultValue = "after") String direction,
//                                          @RequestParam(name = "count", defaultValue = "false", required = false) boolean count) throws JsonProcessingException {
//        if (!direction.equalsIgnoreCase("after")) {
//            return ResponseEntity.ok(messageService.getOldMessagesForUser(id, username, pageable).map(MessageVM::new));
//        }
//
//        if (count) {
//            long newMessagesCount = messageService.getNewMessagesCountForUser(id, username);
//            return ResponseEntity.ok(Collections.singletonMap("count", newMessagesCount));
//        }
//
//        List<MessageVM> newMessages = messageService.getNewMessagesForUser(id, username, pageable).stream()
//                .map(MessageVM::new).collect(Collectors.toList());
//        return ResponseEntity.ok(newMessages);
//    }

    @GetMapping("/messages/{id:[0-9]+}")
    ResponseEntity<?> getMessagesRelative(@CurrentUser User user, @PathVariable long id,
                                        @RequestParam(name = "direction", defaultValue = "after") String direction,
                                        @RequestParam(name = "count", defaultValue = "false", required = false) boolean count,
                                        Pageable pageable) {
        if(direction.equalsIgnoreCase("after")) {
            if(count) {
                long messageCount = messageService.getNewMessagesCountForUser(id, user.getUsername());
                return ResponseEntity.ok(Collections.singletonMap("count", messageCount));
            } else {
                List<MessageVM> messages = messageService.getNewMessagesForUser(id, user.getUsername(), pageable).stream().map(MessageVM::new).collect(Collectors.toList());
                return ResponseEntity.ok(messages);
            }
        }
        Page<Message> messagesBefore = messageService.getOldMessagesForUser(id, user.getUsername(), pageable);
        return ResponseEntity.ok(messagesBefore.map(MessageVM::new));
    }

    @GetMapping("/users/{username}/messages/{id:[0-9]+}")
    ResponseEntity<?> getMessagesRelativeToUser(@PathVariable String username, @PathVariable long id,
                                              @RequestParam(name = "direction", defaultValue = "before") String direction,
                                              Pageable pageable) {
        Page<Message> messagesBefore = messageService.getOldMessagesForUser(id, username, pageable);
        return ResponseEntity.ok(messagesBefore.map(MessageVM::new));
    }



    @DeleteMapping("/messages/{id:[0-9]+}")
    @PreAuthorize("@messageSecurityService.isAllowedToDelete(#id, principal)")
    GenericResponse deleteMessage(@PathVariable long id) {
        messageService.deleteMessage(id);
        return new GenericResponse("Post removed!");
    }
}
