package com.fitclub.fitclub.message.vm;

import com.fitclub.fitclub.file.FileAttachmentVM;
import com.fitclub.fitclub.message.Message;
import com.fitclub.fitclub.user.User;
import com.fitclub.fitclub.user.vm.UserVM;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageVM {

    private long id;

    private String content;

    private long date;

    private UserVM user;

    private FileAttachmentVM attachment;

    public MessageVM(Message message) {
        this.setId(message.getId());
        this.setContent(message.getContent());
        this.setDate(message.getTimestamp().getTime());
        this.setUser(new UserVM(message.getUser()));
        if (message.getAttachment() != null) {
            this.setAttachment(new FileAttachmentVM(message.getAttachment()));
        }
    }
}
