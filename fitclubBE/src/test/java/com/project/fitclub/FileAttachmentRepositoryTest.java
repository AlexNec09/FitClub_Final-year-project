package com.project.fitclub;

import com.project.fitclub.dao.FileAttachmentRepository;
import com.project.fitclub.model.FileAttachment;
import com.project.fitclub.model.Post;
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
    public void findByDateBeforeAndPostIsNull_whenAttachmentsDateOlderThanOneHour_returnsAll() {
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndPostIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(3);
    }

    @Test
    public void findByDateBeforeAndPostIsNull_whenAttachmentsDateOlderThanOneHourButHaveMessage_returnsNone() {
        Post post1 = testEntityManager.persist(TestUtil.createValidPost());
        Post post2 = testEntityManager.persist(TestUtil.createValidPost());
        Post post3 = testEntityManager.persist(TestUtil.createValidPost());

        testEntityManager.persist(getOneHourOldFileAttachmentWithPost(post1));
        testEntityManager.persist(getOneHourOldFileAttachmentWithPost(post2));
        testEntityManager.persist(getOneHourOldFileAttachmentWithPost(post3));
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndPostIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(0);
    }

    @Test
    public void findByDateBeforeAndPostIsNull_whenAttachmentsDateWithinOneHour_returnsNone() {
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndPostIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(0);
    }

    @Test
    public void findByDateBeforeAndPostIsNull_whenSomeAttachmentsOldSomeNewAndSomeWithPost_returnsAttachmentsWithOlderAndNoMessageAssigned() {
        Post post = testEntityManager.persist(TestUtil.createValidPost());

        testEntityManager.persist(getOneHourOldFileAttachmentWithPost(post));
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndPostIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(1);
    }

    private FileAttachment getOneHourOldFileAttachment() {
        Date date = new Date(System.currentTimeMillis() - (60 * 60 * 1000) - 1);
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }

    private FileAttachment getOneHourOldFileAttachmentWithPost(Post post) {
        FileAttachment fileAttachment = getOneHourOldFileAttachment();
        fileAttachment.setPost(post);
        return fileAttachment;
    }

    private FileAttachment getFileAttachmentWithinOneHour() {
        Date date = new Date(System.currentTimeMillis() - (60 * 1000));
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }
}
