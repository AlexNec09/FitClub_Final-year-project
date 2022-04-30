package com.fitclub.fitclub;

import com.fitclub.fitclub.model.Entity.FileAttachment;
import com.fitclub.fitclub.dao.attachment.FileAttachmentRepository;
import com.fitclub.fitclub.model.Entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
public class FileAttachmentRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @Test
    public void findByDateBeforeAndMessageIsNull_whenAttachmentsDateOlderThanOneHour_returnsAll() {
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndMessageIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(3);
    }

    @Test
    public void findByDateBeforeAndMessageIsNull_whenAttachmentsDateOlderThanOneHourButHaveMessage_returnsNone() {
        Message message1 = testEntityManager.persist(TestUtil.createValidMessage());
        Message message2 = testEntityManager.persist(TestUtil.createValidMessage());
        Message message3 = testEntityManager.persist(TestUtil.createValidMessage());

        testEntityManager.persist(getOneHourOldFileAttachmentWithMessage(message1));
        testEntityManager.persist(getOneHourOldFileAttachmentWithMessage(message2));
        testEntityManager.persist(getOneHourOldFileAttachmentWithMessage(message3));
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndMessageIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(0);
    }

    @Test
    public void findByDateBeforeAndMessageIsNull_whenAttachmentsDateWithinOneHour_returnsNone() {
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndMessageIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(0);
    }

    @Test
    public void findByDateBeforeAndMessageIsNull_whenSomeAttachmentsOldSomeNewAndSomeWithMessage_returnsAttachmentsWithOlderAndNoMessageAssigned() {
        Message message1 = testEntityManager.persist(TestUtil.createValidMessage());

        testEntityManager.persist(getOneHourOldFileAttachmentWithMessage(message1));
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndMessageIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(1);
    }

    private FileAttachment getOneHourOldFileAttachment() {
        Date date = new Date(System.currentTimeMillis() - (60 * 60 * 1000) - 1);
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }

    private FileAttachment getOneHourOldFileAttachmentWithMessage(Message message) {
        FileAttachment fileAttachment = getOneHourOldFileAttachment();
        fileAttachment.setMessage(message);
        return fileAttachment;
    }

    private FileAttachment getFileAttachmentWithinOneHour() {
        Date date = new Date(System.currentTimeMillis() - (60 * 1000));
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }
}
