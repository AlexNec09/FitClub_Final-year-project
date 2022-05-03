package com.fitclub.fitclub.service;

import com.fitclub.fitclub.dao.reaction.MessageReactionRepository;
import com.fitclub.fitclub.model.Entity.Message;
import com.fitclub.fitclub.model.Entity.MessageReaction;
import com.fitclub.fitclub.model.Entity.Reaction;
import com.fitclub.fitclub.model.Entity.User;
import org.springframework.stereotype.Service;

@Service
public class MessageReactionService {

    MessageService messageService;

    MessageReactionRepository messageReactionRepository;

    public MessageReactionService(MessageService messageService, MessageReactionRepository messageReactionRepository) {
        super();
        this.messageService = messageService;
        this.messageReactionRepository = messageReactionRepository;
    }

    public void like(long id, User user) {
        react(Reaction.LIKE, id, user);
    }

    public void dislike(long id, User user) {
        react(Reaction.DISLIKE, id, user);
    }

    private void react(Reaction reaction, long id, User user) {
        Message inDB = messageService.getMessage(id);
        MessageReaction reactionInDB = messageReactionRepository.findByMessageAndUser(inDB, user);
        if (reactionInDB == null) {
            MessageReaction messageReaction = new MessageReaction();
            messageReaction.setReaction(reaction);
            messageReaction.setMessage(inDB);
            messageReaction.setUser(user);
            messageReactionRepository.save(messageReaction);
        } else if (reactionInDB.getReaction() == reaction) {
            messageReactionRepository.delete(reactionInDB);
        } else {
            reactionInDB.setReaction(reaction);
            messageReactionRepository.save(reactionInDB);
        }
    }

}
