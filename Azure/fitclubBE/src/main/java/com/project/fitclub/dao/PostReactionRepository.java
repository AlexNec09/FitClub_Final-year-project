package com.project.fitclub.dao;

import com.project.fitclub.model.Post;
import com.project.fitclub.model.PostReaction;
import com.project.fitclub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

    PostReaction findByPostAndUser(Post post, User user);

}
