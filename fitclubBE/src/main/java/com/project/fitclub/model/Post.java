package com.project.fitclub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.persistence.*;
import java.util.*;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Post {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private long id;

    @NotNull
    @Size(min = 2, max = 5000)
    @Column(length = 5000)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @JsonIgnore
    @ManyToOne
    @EqualsAndHashCode.Include
    User user;

    @OneToOne(mappedBy = "post", orphanRemoval = true)
    private FileAttachment attachment;

    @OneToMany(mappedBy = "post", orphanRemoval = true)
    Set<PostReaction> postReactions = new HashSet<>();
}