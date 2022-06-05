package com.project.fitclub.security.payload;

import com.project.fitclub.model.FileAttachment;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class PostRequest {

    @NotNull
    @Size(min = 10, max = 5000)
    @Column(length = 5000)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    private FileAttachment attachment;

    private long userId;

}
