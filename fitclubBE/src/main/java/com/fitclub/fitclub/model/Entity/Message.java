package com.fitclub.fitclub.model.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Message {

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Size(min = 10, max = 5000)
    @Column(length = 5000)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @ManyToOne
//    @ToString.Exclude
    private User user;

    @OneToOne(mappedBy = "message", orphanRemoval = true)
    private FileAttachment attachment;

    @OneToMany(mappedBy = "message", cascade = CascadeType.REMOVE)
    Set<MessageReaction> messageReactions = new HashSet<>();
}
