package com.fitclub.fitclub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitclub.fitclub.dao.message.MessageRepository;
import com.fitclub.fitclub.error.NotFoundHandler;
import com.fitclub.fitclub.model.Entity.FileAttachment;
import com.fitclub.fitclub.dao.attachment.FileAttachmentRepository;
import com.fitclub.fitclub.model.Entity.Message;
import com.fitclub.fitclub.model.Entity.MessageReaction;
import com.fitclub.fitclub.model.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;

@Service
public class MessageService {

    MessageRepository messageRepository;

    UserService userService;

    FileAttachmentRepository fileAttachmentRepository;

    FileService fileService;

    public MessageService(MessageRepository messageRepository, UserService userService,
                          FileAttachmentRepository fileAttachmentRepository, FileService fileService) {
        super();
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.fileService = fileService;
    }

    public Message save(User user, Message message) {
        message.setTimestamp(new Date());
        message.setUser(user);
        if (message.getAttachment() != null) {
            FileAttachment inDB = fileAttachmentRepository.findById(message.getAttachment().getId()).get();
            // I trust that the attachment is existing in db so I won't check if it's present or not
            inDB.setMessage(message);
            message.setAttachment(inDB);
        }
        return messageRepository.save(message);
    }

    public Page<Message> getAllMessages(Pageable pageable) {
        return messageRepository.findAll(pageable);
    }

    public Page<Message> getMessagesOfUser(String username, Pageable pageable) {  // this is showing on user page
        User inDB = userService.getByUsername(username);
        return messageRepository.findByUser(inDB, pageable);
    }

    public Page<Message> getMessagesForUser(Pageable pageable, String username) {
        User inDB = userService.getByUsername(username);
        Set<User> users = inDB.getFollows();
        users.add(inDB);
        return messageRepository.findByUserInOrderByIdDesc(users, pageable);
    }

    public Page<Message> getOldMessages(long id, String username, Pageable pageable) {  // like getOldMessagesGlobal
        Specification<Message> spec = Specification.where(idLessThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return messageRepository.findAll(spec, pageable);
    }

    public List<Message> getNewMessages(long id, String username, Pageable pageable) {  // like getNewMessagesGlobal
        Specification<Message> spec = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return messageRepository.findAll(spec, pageable.getSort());
    }

    public long getNewMessagesCount(long id, String username) {  // like getNewMessagesCountGlobal
        Specification<Message> spec = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return messageRepository.count(spec);
    }

    public Page<Message> getOldMessagesForUser(long id, String username, Pageable pageable) {
        Specification<Message> spec = Specification.where(idLessThan(id));

        if (username != null) {
            User inDB = userService.getByUsername(username);
            Set<User> users = inDB.getFollows();
            users.add(inDB);
            Specification<Message> query = Specification.where(idLessThan(id)).and(getUsersIn(users));
            return messageRepository.findAll(query, pageable);
        }
        return messageRepository.findAll(spec, pageable);
    }

    public List<Message> getNewMessagesForUser(long id, String username, Pageable pageable) {
        Specification<Message> spec = Specification.where(idGreaterThan(id));

        if (username != null) {
            User inDB = userService.getByUsername(username);
            Set<User> users = inDB.getFollows();
            users.add(inDB);
            Specification<Message> query = Specification.where(idGreaterThan(id)).and(getUsersIn(users));
            return messageRepository.findAll(query, pageable.getSort());
        }
        return messageRepository.findAll(spec, pageable.getSort());
    }

    public long getNewMessagesCountForUser(long id, String username) {
        Specification<Message> spec = Specification.where(idGreaterThan(id));

        if (username != null) {
            User inDB = userService.getByUsername(username);
            Set<User> users = inDB.getFollows();
            users.add(inDB);
            Specification<Message> query = Specification.where(idGreaterThan(id)).and(getUsersIn(users));
            return messageRepository.count(query);
        }
        return messageRepository.count(spec);
    }

    public Message getMessage(long id) {
        return messageRepository.findById(id).orElseThrow(() -> new NotFoundHandler("Post not found!"));
    }

    private Specification<Message> getUsersIn(Set<User> users) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get("id")));
            CriteriaBuilder.In<User> inResult = criteriaBuilder.in(root.get("user"));
            users.forEach(inResult::value);
            return inResult;
        };
    }

    private Specification<Message> userIs(User user) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
    }

    private Specification<Message> idLessThan(long id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("id"), id);
    }

    private Specification<Message> idGreaterThan(long id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("id"), id);
    }

    public void deleteMessage(long id) {
        Optional<Message> optionalMessage = messageRepository.findById(id);
        if (optionalMessage.isPresent()) {
            Message foundMessage = optionalMessage.get();
            if (foundMessage.getAttachment() != null) {
                fileService.deleteAttachmentImage(foundMessage.getAttachment().getName());
            }
        }
        messageRepository.deleteById(id);
    }
}
