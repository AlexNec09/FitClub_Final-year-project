package com.project.fitclub.model.vm;

import com.project.fitclub.model.MessageReaction;
import com.project.fitclub.model.Reaction;
import com.project.fitclub.shared.UserSecurityUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ReactionVM {

    private long likeCount;

    private long dislikeCount;

    private Reaction loggedUserReaction;

    public ReactionVM(Set<MessageReaction> reactions) {

        Map<Reaction, Long> count = reactions.stream()
                .peek(this::currentUserReaction)
                .collect(Collectors.groupingBy(MessageReaction::getReaction, Collectors.counting()));

        this.setLikeCount(count.getOrDefault(Reaction.LIKE, 0L));
        this.setDislikeCount(count.getOrDefault(Reaction.DISLIKE, 0L));
    }

    private void currentUserReaction(MessageReaction messageReaction) {
        if (this.loggedUserReaction == null && messageReaction.getUser().equals(UserSecurityUtil.getLoggedInUser())) {
            setLoggedUserReaction(messageReaction.getReaction());
        }
    }

}
