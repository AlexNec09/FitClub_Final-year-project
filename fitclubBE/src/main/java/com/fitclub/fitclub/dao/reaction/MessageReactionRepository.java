package com.fitclub.fitclub.dao.reaction;

import com.fitclub.fitclub.model.Entity.Message;
import com.fitclub.fitclub.model.Entity.MessageReaction;
import com.fitclub.fitclub.model.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    MessageReaction findByMessageAndUser(Message message, User user);

}
