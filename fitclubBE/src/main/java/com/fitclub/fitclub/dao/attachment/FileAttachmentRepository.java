package com.fitclub.fitclub.dao.attachment;

import com.fitclub.fitclub.model.Entity.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    List<FileAttachment> findByDateBeforeAndMessageIsNull(Date date);
}
