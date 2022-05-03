package com.fitclub.fitclub.model;

import com.fitclub.fitclub.model.Entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserVM {

    private long id;
    private String username;
    private String displayName;
    private String image;

    int follows;

    int followedBy;

    boolean followed;

    public UserVM(User user) {
        this.setId(user.getId());
        this.setUsername(user.getUsername());
        this.setDisplayName(user.getDisplayName());
        this.setImage(user.getImage());
        this.setFollows(user.getFollows().size());
        this.setFollowedBy(user.getFollowedBy().size());
    }

    public UserVM(User user, User loggedInUser) {
        this(user);
        this.setFollowed(user.getFollowedBy().contains(loggedInUser));
    }

    public static UserVM createUserVM(User user) {
        UserVM vm = new UserVM();
        vm.setId(user.getId());
        vm.setUsername(user.getUsername());
        vm.setDisplayName(user.getDisplayName());
        vm.setImage(user.getImage());
        return vm;
    }
}
