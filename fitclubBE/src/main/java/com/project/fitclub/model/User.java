package com.project.fitclub.model;

import com.project.fitclub.validation.UniqueUsername;
import com.project.fitclub.validation.verificationToken.VerificationToken;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.beans.Transient;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
public class User extends DateAudit {

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

    @NaturalId(mutable=true)
    @Email
    @Size(min = 6, max = 50)
    @NotNull
    private String email;

    @NotNull
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "{fitclub.constraints.password.Pattern.message}")
    String password;

    String image;

    @OneToOne(mappedBy = "user", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private VerificationToken verificationToken;

    @NotNull
    private Boolean emailVerificationStatus = false;

    @OneToMany(mappedBy = "user")
    List<Post> posts = new ArrayList<>();

    @ManyToMany
    Set<User> followedBy = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    Set<User> follows = new HashSet<>();

    public User(String displayName, String username, String password) {
        this.displayName = displayName;
        this.username = username;
        this.password = password;
    }
}