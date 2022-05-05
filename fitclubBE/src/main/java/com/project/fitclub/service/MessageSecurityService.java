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

    public boolean isAllowedToDelete(User user, long messageId) {
        Optional<Message> optMessage = messageRepository.findById(messageId);
        if (optMessage.isPresent()) {
            Message inDB = optMessage.get();
            return user.equals(inDB.getUser());
        }
        return false;
    }
}
