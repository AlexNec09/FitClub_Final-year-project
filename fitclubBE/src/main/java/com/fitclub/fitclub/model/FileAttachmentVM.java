package com.fitclub.fitclub.model;

import com.fitclub.fitclub.model.Entity.FileAttachment;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileAttachmentVM {

    private String name;

    private String fileType;

    public FileAttachmentVM(FileAttachment fileAttachment) {
        this.setName(fileAttachment.getName());
        this.setFileType(fileAttachment.getFileType());
    }
}
