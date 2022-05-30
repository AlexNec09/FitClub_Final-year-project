package com.project.fitclub.dao;

import com.project.fitclub.model.Message;
import com.project.fitclub.model.MessageReaction;
import com.project.fitclub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    MessageReaction findByMessageAndUser(Message message, User user);

}
