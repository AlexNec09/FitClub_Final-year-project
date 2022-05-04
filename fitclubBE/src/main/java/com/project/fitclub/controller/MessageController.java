package com.project.fitclub.controller;

import com.project.fitclub.model.Message;
import com.project.fitclub.model.User;
import com.project.fitclub.model.vm.MessageVM;
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

    @GetMapping("/messages")
    Page<?> getAllMessages(Pageable pageable, @CurrentUser User user) {
        if (user != null)
            return messageService.getMessagesForUser(pageable, user).map(MessageVM::new);
        return messageService.getAllMessages(pageable).map(MessageVM::new);
    }

    @GetMapping("/users/{username}/messages")
    Page<?> getMessagesOfUser(@PathVariable String username, Pageable pageable) {
        return messageService.getMessagesOfUser(username, pageable).map(MessageVM::new);
    }

    @PostMapping("/messages")
    MessageVM createMessage(@Valid @RequestBody Message message, @CurrentUser User user) {
        return new MessageVM(messageService.save(user, message));
    }

    @DeleteMapping("/messages/{id:[0-9]+}")
    @PreAuthorize("@messageSecurityService.isAllowedToDelete(#user, #id)")
    GenericResponse deleteMessage(@CurrentUser User user, @PathVariable long id) {
        messageService.deleteMessage(id);
        return new GenericResponse("Message is removed");
    }

//    @GetMapping("/messages/{id:[0-9]+}")
//    ResponseEntity<?> getMessagesRelative(@CurrentUser User user, @PathVariable long id,
//                                          @RequestParam(name = "direction", defaultValue = "after") String direction,
//                                          @RequestParam(name = "count", defaultValue = "false", required = false) boolean count,
//                                          Pageable pageable) {
//        if (direction.equalsIgnoreCase("after")) {
//            if (count) {
//                long messageCount = messageService.countMessagesAfter(id, user);
//                return ResponseEntity.ok(Collections.singletonMap("count", messageCount));
//            } else {
//                List<MessageVM> messages = messageService.getMessagesAfter(id, user).stream().map(MessageVM::new).collect(Collectors.toList());
//                return ResponseEntity.ok(messages);
//            }
//        }
//        Page<Message> messagesBefore = messageService.getMessagesBefore(id, user, pageable);
//        return ResponseEntity.ok(messagesBefore.map(MessageVM::new));
//    }
//
//    @GetMapping("/users/{username}/messages/{id:[0-9]+}")
//    ResponseEntity<?> getMessagesRelativeToUser(@PathVariable String username, @PathVariable long id,
//                                                @RequestParam(name = "direction", defaultValue = "before") String direction,
//                                                Pageable pageable) {
//        Page<Message> messagesBefore = messageService.getMessagesBeforeForUser(id, username, pageable);
//        return ResponseEntity.ok(messagesBefore.map(MessageVM::new));
//    }


        @GetMapping({"/messages/{id:[0-9]+}", "/users/{username}/messages/{id:[0-9]+}"})
    ResponseEntity<?> getMessagesRelative(@PathVariable long id,
                                          @PathVariable(required = false) String username,
                                          Pageable pageable,
                                          @RequestParam(name = "direction", defaultValue = "after") String direction,
                                          @RequestParam(name = "count", defaultValue = "false", required = false) boolean count) {
        if (!direction.equalsIgnoreCase("after")) {
            return ResponseEntity.ok(messageService.getMessagesBefore(id, username, pageable).map(MessageVM::new));
        }

        if (count) {
            long newMessagesCount = messageService.countMessagesAfter(id, username);
            return ResponseEntity.ok(Collections.singletonMap("count", newMessagesCount));
        }

        List<MessageVM> newMessages = messageService.getMessagesAfter(id, username).stream()
                .map(MessageVM::new).collect(Collectors.toList());
        return ResponseEntity.ok(newMessages);
    }

}