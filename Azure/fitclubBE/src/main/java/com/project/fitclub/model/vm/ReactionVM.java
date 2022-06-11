package com.project.fitclub.model.vm;

import com.project.fitclub.model.PostReaction;
import com.project.fitclub.model.Reaction;
import com.project.fitclub.security.UserPrincipal;
import com.project.fitclub.shared.UserSecurityUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ReactionVM {

    private long likeCount;

    private long dislikeCount;

    private Reaction loggedUserReaction;

    public ReactionVM(Set<PostReaction> reactions) {

        Map<Reaction, Long> count = reactions.stream()
                .peek(this::currentUserReaction)
                .collect(Collectors.groupingBy(PostReaction::getReaction, Collectors.counting()));

        this.setLikeCount(count.getOrDefault(Reaction.LIKE, 0L));
        this.setDislikeCount(count.getOrDefault(Reaction.DISLIKE, 0L));
    }

    private void currentUserReaction(PostReaction postReaction) {
        String principalUsername = null;

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            principalUsername = ((UserDetails) principal).getUsername();
        }
        if (this.loggedUserReaction == null && postReaction.getUser().getUsername().equals(principalUsername)) {
            setLoggedUserReaction(postReaction.getReaction());
        }
    }
}
