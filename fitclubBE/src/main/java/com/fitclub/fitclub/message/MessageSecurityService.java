package com.fitclub.fitclub.message;

import com.fitclub.fitclub.user.User;
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
