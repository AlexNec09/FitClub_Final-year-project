package com.project.fitclub.validation.verificationToken;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.fitclub.model.User;
import lombok.*;

import javax.persistence.*;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Getter
@Setter
public class VerificationToken {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private long id;

    private String emailToken;

    private String passwordToken;

    @OneToOne
    @JsonIgnore
    @EqualsAndHashCode.Include
    private User user;

    public VerificationToken(User user) {
        this.user = user;
    }
}
