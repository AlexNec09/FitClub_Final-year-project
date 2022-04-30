package com.fitclub.fitclub;

import com.fitclub.fitclub.configuration.AppConfiguration;
import com.fitclub.fitclub.model.Entity.FileAttachment;
import com.fitclub.fitclub.dao.attachment.FileAttachmentRepository;
import com.fitclub.fitclub.service.FileService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class FileServiceTest {
    FileService fileService;

    AppConfiguration appConfiguration;

    @MockBean
    FileAttachmentRepository fileAttachmentRepository;

    @BeforeEach
    public void init() {
        appConfiguration = new AppConfiguration();
        appConfiguration.setUploadPath("uploads-test");

        fileService = new FileService(appConfiguration, fileAttachmentRepository);

        new File(appConfiguration.getUploadPath()).mkdir();
        new File(appConfiguration.getFullProfileImagesPath()).mkdir();
        new File(appConfiguration.getFullAttachmentsPath()).mkdir();
    }

    @Test
    public void detectType_whenPngFileProvided_returnsImagePng() throws IOException {
        ClassPathResource resourceFile = new ClassPathResource("test-png.png");
        byte[] fileArr = FileUtils.readFileToByteArray(resourceFile.getFile());
        String fileType = fileService.detectType(fileArr);
        assertThat(fileType).isEqualToIgnoringCase("image/png");
    }

    @Test
    public void cleanupStorage_whenOldFilesExist_removesFilesFromStorage() throws IOException {
        String fileName = "random-file";
        String filePath = appConfiguration.getFullAttachmentsPath() + "/" + fileName;
        File source = new ClassPathResource("test-png.png").getFile();
        File target = new File(filePath);
        FileUtils.copyFile(source, target);

        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setId(5);
        fileAttachment.setName(fileName);

        Mockito.when(fileAttachmentRepository.findByDateBeforeAndMessageIsNull(Mockito.any(Date.class)))
                .thenReturn(Arrays.asList(fileAttachment));

        fileService.cleanupStorage();
        File storedImage = new File(filePath);
        assertThat(storedImage.exists()).isFalse();
    }

    @Test
    public void cleanupStorage_whenOldFilesExist_removesFileAttachmentFromDatabase() throws IOException {
        String fileName = "random-file";
        String filePath = appConfiguration.getFullAttachmentsPath() + "/" + fileName;
        File source = new ClassPathResource("test-png.png").getFile();
        File target = new File(filePath);
        FileUtils.copyFile(source, target);

        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setId(5);
        fileAttachment.setName(fileName);

        Mockito.when(fileAttachmentRepository.findByDateBeforeAndMessageIsNull(Mockito.any(Date.class)))
                .thenReturn(Arrays.asList(fileAttachment));

        fileService.cleanupStorage();
        Mockito.verify(fileAttachmentRepository).deleteById(5L);
    }

    @AfterEach
    public void cleanup() throws IOException {
        FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
    }
}
