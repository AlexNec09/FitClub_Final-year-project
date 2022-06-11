package com.project.fitclub.service;

import com.project.fitclub.dao.PostRepository;
import com.project.fitclub.model.Post;
import com.project.fitclub.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostSecurityService {

    PostRepository postRepository;

    public PostSecurityService(PostRepository postRepository) {
        super();
        this.postRepository = postRepository;
    }

    public boolean isAllowedToDelete(UserPrincipal userPrincipal, long postId) {
        Optional<Post> optionalHoax = postRepository.findById(postId);
        if (optionalHoax.isPresent()) {
            Post inDB = optionalHoax.get();
            return inDB.getUser().getId() == userPrincipal.getId();
        }
        return false;
    }
}
