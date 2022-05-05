package com.project.fitclub.model;

import com.project.fitclub.validation.UniqueUsername;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.beans.Transient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements UserDetails {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    long id;

    @NotNull(message = "{fitclub.constraints.username.NotNull.message}")
    @Size(min = 4, max = 255)
    @UniqueUsername
    @EqualsAndHashCode.Include
    String username;

    @NotNull
    @Size(min = 4, max = 255)
    String displayName;

    @NotNull
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{fitclub.constraints.password.Pattern.message}")
    String password;

    String image;

    @OneToMany(mappedBy = "user")
    List<Message> messages = new ArrayList<>();

    @ManyToMany
    Set<User> followedBy = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    Set<User> follows = new HashSet<>();

    public User() {
        super();
    }

    @Override
    @Transient
//    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("Role_USER");
    }

    @Override
//    @JsonIgnore
    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
//    @JsonIgnore
    @Transient
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
//    @JsonIgnore
    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
//    @JsonIgnore
    @Transient
    public boolean isEnabled() {
        return true;
    }
}