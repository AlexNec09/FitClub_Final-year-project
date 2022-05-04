package com.project.fitclub.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Data
public class MessageReaction {
    @Id
    @GeneratedValue
    private long id;

    private Reaction reaction;

    @ManyToOne
    User user;

    @ManyToOne
    Message message;

}
