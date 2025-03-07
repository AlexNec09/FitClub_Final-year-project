package com.project.fitclub.service;

import com.project.fitclub.configuration.AppConfiguration;
import com.project.fitclub.dao.FileAttachmentRepository;
import com.project.fitclub.model.FileAttachment;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@EnableScheduling
public class FileService {

    AppConfiguration appConfiguration;

    Tika tika;

    FileAttachmentRepository fileAttachmentRepository;

    public FileService(AppConfiguration appConfiguration, FileAttachmentRepository fileAttachmentRepository) {
        super();
        this.appConfiguration = appConfiguration;
        this.fileAttachmentRepository = fileAttachmentRepository;
        tika = new Tika();
    }

    public String saveProfileImage(String base64Image) throws IOException {
        String imageName = getRandomName();

        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
        File target = new File(appConfiguration.getFullProfileImagesPath() + "/" + imageName);
        FileUtils.writeByteArrayToFile(target, decodedBytes);
        return imageName;
    }

    private String getRandomName() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public FileAttachment findByName(String fileName) {
        return fileAttachmentRepository.findByName(fileName);
    }

    public String detectType(byte[] fileArr) {
        return tika.detect(fileArr);
    }

    public void deleteProfileImage(String image) {
        try {
            Files.deleteIfExists(Paths.get(appConfiguration.getFullProfileImagesPath() + "/" + image));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileAttachment saveAttachment(MultipartFile file) throws IOException {
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(new Date());
        String randomName = getRandomName();
        fileAttachment.setName(randomName);

        File target = new File(appConfiguration.getFullAttachmentsPath() + "/" + randomName);
        byte[] fileAsByte = file.getBytes();
        FileUtils.writeByteArrayToFile(target, fileAsByte);
        String fileType = detectType(fileAsByte);
        if (fileType.equalsIgnoreCase("image/png") ||
                fileType.equalsIgnoreCase("image/jpeg") || fileType.equalsIgnoreCase("image/gif")) {
            fileAttachment.setFileType(fileType);
        } else {
            throw new IOException("Only PNG, JPG and GIF files are allowed!");
        }

        return fileAttachmentRepository.save(fileAttachment);
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanupStorage() {
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> oldFilesWithNoPost = fileAttachmentRepository.findByDateBeforeAndPostIsNull(oneHourAgo);
        for (FileAttachment file : oldFilesWithNoPost) {
            if (file.getPost() == null) {
                deleteAttachmentImage(file.getName());
                fileAttachmentRepository.deleteById(file.getId());
            }
        }
    }

    public void deleteAttachmentImage(String image) {
        try {
            Files.deleteIfExists(Paths.get(appConfiguration.getFullAttachmentsPath() + "/" + image));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
