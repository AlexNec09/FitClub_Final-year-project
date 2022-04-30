package com.fitclub.fitclub.model.Entity;

import com.fitclub.fitclub.model.Entity.Message;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class FileAttachment {

    @Id
    @GeneratedValue
    private long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    private String name;

    private String fileType;

    @OneToOne
    private Message message;
}
