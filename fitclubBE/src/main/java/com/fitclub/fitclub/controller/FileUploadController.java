package com.fitclub.fitclub.controller;

import com.fitclub.fitclub.service.FileService;
import com.fitclub.fitclub.model.Entity.FileAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/1.0")
public class FileUploadController {

    @Autowired
    FileService fileService;

    @PostMapping("/messages/upload")
    FileAttachment uploadForMessage(MultipartFile file) {
        return fileService.saveAttachment(file);
    }
}
