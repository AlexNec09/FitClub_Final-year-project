package com.project.fitclub.service;

import com.project.fitclub.dao.MessageRepository;
import com.project.fitclub.model.Message;
import com.project.fitclub.model.User;
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
