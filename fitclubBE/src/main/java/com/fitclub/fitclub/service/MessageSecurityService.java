package com.fitclub.fitclub.service;

import com.fitclub.fitclub.dao.message.MessageRepository;
import com.fitclub.fitclub.model.Entity.Message;
import com.fitclub.fitclub.model.Entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessageSecurityService {

    MessageRepository messageRepository;

    public MessageSecurityService(MessageRepository messageRepository) {
        super();
        this.messageRepository = messageRepository;
    }

    public boolean isAllowedToDelete(long messageId, User loggedInUser) {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isPresent()) {
            Message inDB = optionalMessage.get();
            return inDB.getUser().getId() == loggedInUser.getId();
        }
        return false;
    }
}
