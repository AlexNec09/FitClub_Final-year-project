package com.fitclub.fitclub.message;

import com.fitclub.fitclub.file.FileAttachment;
import com.fitclub.fitclub.file.FileAttachmentRepository;
import com.fitclub.fitclub.file.FileService;
import com.fitclub.fitclub.user.User;
import com.fitclub.fitclub.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    public Page<Message> getOldMessages(long id, String username, Pageable pageable) {
        Specification<Message> spec = Specification.where(idLessThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return messageRepository.findAll(spec, pageable);
    }

    public List<Message> getNewMessages(long id, String username, Pageable pageable) {
        Specification<Message> spec = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return messageRepository.findAll(spec, pageable.getSort());
    }

    public long getNewMessagesCount(long id, String username) {
        Specification<Message> spec = Specification.where(idGreaterThan(id));
        if (username != null) {
            User inDB = userService.getByUsername(username);
            spec = spec.and(userIs(inDB));
        }
        return messageRepository.count(spec);
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
