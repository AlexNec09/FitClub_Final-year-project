package com.project.fitclub.service;

import com.project.fitclub.dao.FileAttachmentRepository;
import com.project.fitclub.dao.MessageRepository;
import com.project.fitclub.error.NotFoundHandler;
import com.project.fitclub.model.FileAttachment;
import com.project.fitclub.model.Message;
import com.project.fitclub.model.User;
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

    public Page<Message> getMessagesOfUser(String username, Pageable pageable) {
        User inDB = userService.getByUsername(username);
        return messageRepository.findByUser(inDB, pageable);
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

    public Message getMessage(long id) {
        return messageRepository.findById(id).orElseThrow(() -> new NotFoundHandler("Message not found!"));
    }

    public Page<Message> getMessagesForUser(Pageable pageable, User user) {
        User forUser = userService.getByUsername(user.getUsername());
        Set<User> users = forUser.getFollows();
        users.add(forUser);
        return messageRepository.findByUserInOrderByIdDesc(users, pageable);
    }

    private Specification<Message> getUsersIn(Set<User> users) {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("id")));
            CriteriaBuilder.In<User> inResult = cb.in(root.get("user"));
            users.forEach(u -> inResult.value(u));
            return inResult;
        };
    }

    private Specification<Message> getMessagesAfter(long id) {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("id")));
            return cb.greaterThan(root.get("id"), id);
        };
    }


    public long countMessagesAfter(long id, String username) {
        User forUser = userService.getByUsername(username);
        Set<User> users = forUser.getFollows();
        users.add(forUser);

        Specification<Message> query = Specification.where(getMessagesAfter(id)).and(getUsersIn(users));
        return messageRepository.count(query);
    }

    public List<Message> getMessagesAfter(long id, String username) {
        User forUser = userService.getByUsername(username);
        Set<User> users = forUser.getFollows();
        users.add(forUser);

        Specification<Message> query = Specification.where(getMessagesAfter(id)).and(getUsersIn(users));
        return messageRepository.findAll(query);
    }

    public Page<Message> getMessagesBefore(long id, String username, Pageable pageable) {
        User forUser = userService.getByUsername(username);
        Set<User> users = forUser.getFollows();
        users.add(forUser);

        Specification<Message> query = Specification.where(getMessagesBefore(id)).and(getUsersIn(users));
        return messageRepository.findAll(query, pageable);
    }

    private Specification<Message> getMessagesBefore(long id) {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("id")));
            return cb.lessThan(root.get("id"), id);
        };
    }

    public Page<Message> getMessagesBeforeForUser(long id, String username, Pageable pageable) {
        User forUser = userService.getByUsername(username);
        Set<User> users = new HashSet<>();
        users.add(forUser);

        Specification<Message> query = Specification.where(getMessagesBefore(id)).and(getUsersIn(users));
        return messageRepository.findAll(query, pageable);
    }

}