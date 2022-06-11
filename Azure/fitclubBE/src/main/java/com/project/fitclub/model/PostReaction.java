package com.project.fitclub.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class PostReaction {
    @Id
    @GeneratedValue
    private long id;

    private Reaction reaction;

    @ManyToOne
    User user;

    @ManyToOne
    Post post;
}
