package com.project.fitclub.model.vm;

import com.project.fitclub.shared.ProfileImage;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserUpdateVM {

    @NotNull
    @Size(min=4, max=255)
    private String displayName;

    @ProfileImage
    private String image;
}
