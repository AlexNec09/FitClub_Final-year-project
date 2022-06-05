package com.project.fitclub.service;

import com.project.fitclub.dao.PostReactionRepository;
import com.project.fitclub.model.Post;
import com.project.fitclub.model.PostReaction;
import com.project.fitclub.model.Reaction;
import com.project.fitclub.model.User;
import org.springframework.stereotype.Service;

@Service
public class PostReactionService {

    PostService postService;

    PostReactionRepository postReactionRepository;

    public PostReactionService(PostService postService, PostReactionRepository postReactionRepository) {
        super();
        this.postService = postService;
        this.postReactionRepository = postReactionRepository;
    }

    public void like(long id, User user) {
        react(Reaction.LIKE, id, user);
    }

    public void dislike(long id, User user) {
        react(Reaction.DISLIKE, id, user);
    }

    private void react(Reaction reaction, long id, User user) {
        Post inDB = postService.getPost(id);
        PostReaction reactionInDB = postReactionRepository.findByPostAndUser(inDB, user);
        if (reactionInDB == null) {
            PostReaction postReaction = new PostReaction();
            postReaction.setReaction(reaction);
            postReaction.setPost(inDB);
            postReaction.setUser(user);
            postReactionRepository.save(postReaction);
        } else if (reactionInDB.getReaction() == reaction) {
            postReactionRepository.delete(reactionInDB);
        } else {
            reactionInDB.setReaction(reaction);
            postReactionRepository.save(reactionInDB);
        }
    }

}