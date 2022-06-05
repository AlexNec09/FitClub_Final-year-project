package com.project.fitclub.service;

import com.project.fitclub.dao.FileAttachmentRepository;
import com.project.fitclub.dao.PostRepository;
import com.project.fitclub.error.NotFoundHandler;
import com.project.fitclub.model.FileAttachment;
import com.project.fitclub.model.Post;
import com.project.fitclub.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;

@Service
public class PostService {

    PostRepository postRepository;

    UserService userService;

    FileAttachmentRepository fileAttachmentRepository;

    FileService fileService;

    public PostService(PostRepository postRepository, UserService userService,
                       FileAttachmentRepository fileAttachmentRepository, FileService fileService) {
        super();
        this.postRepository = postRepository;
        this.userService = userService;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.fileService = fileService;
    }

    public Post save(User user, Post post) {
        post.setTimestamp(new Date());
        post.setUser(user);
        if (post.getAttachment() != null) {
            FileAttachment inDB = fileAttachmentRepository.findById(post.getAttachment().getId()).get();
            inDB.setPost(post);
            post.setAttachment(inDB);
        }
        return postRepository.save(post);
    }

    public Page<Post> getAllMessages(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Page<Post> getPostsOfUser(String username, Pageable pageable) {
        User inDB = userService.getByUsername(username);
        return postRepository.findByUser(inDB, pageable);
    }

    public void deletePost(long id) {
        Optional<Post> optionalMessage = postRepository.findById(id);
        if (optionalMessage.isPresent()) {
            Post foundMessage = optionalMessage.get();
            if (foundMessage.getAttachment() != null) {
                fileService.deleteAttachmentImage(foundMessage.getAttachment().getName());
            }
        }
        postRepository.deleteById(id);
    }

    public Post getPost(long id) {
        return postRepository.findById(id).orElseThrow(() -> new NotFoundHandler("Post not found!"));
    }

    public Page<Post> getPostsForUser(Pageable pageable, Long id) {
        User forUser = userService.getById(id);
        Set<User> users = forUser.getFollows();
        users.add(forUser);
        return postRepository.findByUserInOrderByIdDesc(users, pageable);
    }


    public long countPostsAfter(long id, String username, User loggedInUser) {
        Set<User> users = new HashSet<>();
        if (username != null) {
            User forUser = userService.getByUsername(username);
            users.add(forUser);
        } else {
            User forUser = userService.getByUsername(loggedInUser.getUsername());
            users = forUser.getFollows();
            users.add(forUser);
        }

        Specification<Post> query = Specification.where(getPostsAfter(id)).and(getUsersIn(users));
        return postRepository.count(query);
    }

    public List<Post> getPostsAfter(long id, String username, User loggedInUser, Pageable pageable) {
        Specification<Post> spec = Specification.where(getPostsAfter(id));
        spec = handleUsersSet(username, loggedInUser, spec);

        return postRepository.findAll(spec, pageable.getSort());
    }

    public Page<Post> getPostsBefore(long id, String username, User loggedInUser, Pageable pageable) {
        Specification<Post> spec = Specification.where(getPostsBefore(id));
        spec = handleUsersSet(username, loggedInUser, spec);
        return postRepository.findAll(spec, pageable);
    }

    private Specification<Post> getUsersIn(Set<User> users) {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("id")));
            CriteriaBuilder.In<User> inResult = cb.in(root.get("user"));
            users.forEach(inResult::value);
            return inResult;
        };
    }

    private Specification<Post> handleUsersSet(String username, User loggedInUser, Specification<Post> spec) {
        Set<User> users = new HashSet<>();
        if (username != null) {
            User forUser = userService.getByUsername(username);
            users.add(forUser);
            spec = spec.and(getUsersIn(users));
        } else {
            users = loggedInUser.getFollows();
            users.add(loggedInUser);
            spec = spec.and(getUsersIn(users));
        }
        return spec;
    }

    private Specification<Post> getPostsAfter(long id) {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("id")));
            return cb.greaterThan(root.get("id"), id);
        };
    }

    private Specification<Post> getPostsBefore(long id) {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("id")));
            return cb.lessThan(root.get("id"), id);
        };
    }

}