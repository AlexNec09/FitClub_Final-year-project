package com.fitclub.fitclub.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fitclub")
@Data
public class AppConfiguration {

    String uploadPath;
    String profileImagesFolder = "profile";
    String attachmentsFolder = "attachments";

    public String getFullProfileImagesPath() {
        return this.uploadPath + "/" + this.profileImagesFolder;
    }

    public String getFullAttachmentsPath() {
        return this.uploadPath + "/" + this.attachmentsFolder;
    }
}
