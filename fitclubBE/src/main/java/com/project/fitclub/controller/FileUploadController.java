package com.project.fitclub.controller;

import com.project.fitclub.model.FileAttachment;
import com.project.fitclub.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/1.0")
public class FileUploadController {

    @Autowired
    FileService fileService;

    @PostMapping("/posts/upload")
    FileAttachment uploadForUserPost(MultipartFile file) throws IOException {
        return fileService.saveAttachment(file);
    }
}
