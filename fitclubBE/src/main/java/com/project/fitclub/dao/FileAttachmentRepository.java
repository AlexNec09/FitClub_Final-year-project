package com.project.fitclub.dao;

import com.project.fitclub.model.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    FileAttachment findByName(String name);

    List<FileAttachment> findByDateBeforeAndPostIsNull(Date date);
}
