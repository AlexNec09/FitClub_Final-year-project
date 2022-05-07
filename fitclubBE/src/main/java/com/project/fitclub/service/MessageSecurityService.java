package com.project.fitclub.service;

import com.project.fitclub.dao.MessageRepository;
import com.project.fitclub.model.Message;
import com.project.fitclub.model.User;
import com.project.fitclub.shared.response.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessageSecurityService {

    MessageRepository messageRepository;

    public MessageSecurityService(MessageRepository messageRepository) {
        super();
        this.messageRepository = messageRepository;
    }

    public boolean isAllowedToDelete(@AuthenticationPrincipal UserPrincipal userPrincipal, long messageId) {
        Optional<Message> optionalHoax = messageRepository.findById(messageId);
        if (optionalHoax.isPresent()) {
            Message inDB = optionalHoax.get();
            return inDB.getUser().getId() == userPrincipal.getId();
        }
        return false;
    }
}
