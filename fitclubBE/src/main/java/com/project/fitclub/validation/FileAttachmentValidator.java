package com.project.fitclub.validation;

import com.project.fitclub.service.FileService;
import com.project.fitclub.shared.PostAttachment;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Base64;

public class FileAttachmentValidator implements ConstraintValidator<PostAttachment, String> {

    @Autowired
    FileService fileService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        byte[] decodedBytes = Base64.getDecoder().decode(value);
        String fileType = fileService.detectType(decodedBytes);
        if (fileType.equalsIgnoreCase("image/png") ||
                fileType.equalsIgnoreCase("image/jpeg") || fileType.equalsIgnoreCase("image/gif")) {
            return true;
        }
        return false;
    }
}