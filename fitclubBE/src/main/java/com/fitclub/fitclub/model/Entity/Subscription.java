package com.fitclub.fitclub.model.Entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class Subscription {

    @Id
    @GeneratedValue
    private long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 100, nullable = false)
    private String subtitle;

    @Column(nullable = false)
    private double price;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

}
