package com.project.fitclub.model.vm;

import com.project.fitclub.model.Post;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostVM {

    private long id;

    private String content;

    private long date;

    private UserVM user;

    private FileAttachmentVM attachment;

    private ReactionVM reactions;

    public PostVM(Post post) {
        this.setId(post.getId());
        this.setContent(post.getContent());
        this.setDate(post.getTimestamp().getTime());
        this.setUser(UserVM.createUserVM(post.getUser()));
        if (post.getAttachment() != null) {
            this.setAttachment(new FileAttachmentVM(post.getAttachment()));
        }
        this.setReactions(new ReactionVM(post.getPostReactions()));
    }

}