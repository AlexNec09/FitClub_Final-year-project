package com.project.fitclub.shared;

import com.project.fitclub.model.User;
import com.project.fitclub.model.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    final String SUBJECT_EMAIL_CONFIRMATION = "Confirm your email on FitClub platform";

    final String SUBJECT_CHANGE_EMAIL = "Change your email on FitClub platform";

    final String SUBJECT_RESET_PASSWORD = "Reset your password on FitClub platform";

    final String SUBJECT_CHANGE_PASSWORD = "Change your password on FitClub platform";

    final String TEXTBODY_EMAIL_CONFIRMATION = "Hi, $fullName!" + System.lineSeparator()
            + System.lineSeparator()
            + "In order to gain access to all the resources available, "
            + "you need to confirm your email address by opening the following URL in your browser window:" + System.lineSeparator()
            + " http://fitclub.azurewebsites.net/#/verification/confirmationToken?token=$tokenValue";

    final String TEXTBODY_CHANGE_EMAIL = "Hi, $fullName!" + System.lineSeparator()
            + System.lineSeparator()
            + "You're receiving this email because you requested to change your email on FitClub platform." + System.lineSeparator()
            + "If you did not request this, please disregard this email." + System.lineSeparator()
            + System.lineSeparator()
            + "This confirmation link is valid for 1 hour and can be used only once."
            + " Please be aware that you must confirm your new email address after you have made the change. "
            + "If you would like to continue and change it, please click the following link:" + System.lineSeparator()
            + " http://fitclub.azurewebsites.net/#/verification/changeEmail?token=$tokenValue";

    final String TEXTBODY_PASSWORD_RESET = "Hi, $fullName! " + System.lineSeparator()
            + System.lineSeparator()
            + "You're receiving this email because you requested to reset your password on FitClub platform." + System.lineSeparator()
            + "If you did not request this, please disregard this email." + System.lineSeparator()
            + System.lineSeparator()
            + "This confirmation link is valid for 1 hour and can be used only once."
            + "If you would like to continue and change it, please open the following URL in your browser window:" + System.lineSeparator()
            + " http://fitclub.azurewebsites.net/#/verification/passwordReset?token=$tokenValue";

    final String TEXTBODY_CHANGE_PASSWORD = "Hi, $fullName! " + System.lineSeparator()
            + System.lineSeparator()
            + "You're receiving this email because you requested to change your password on FitClub platform." + System.lineSeparator()
            + "If you did not request this, please disregard this email." + System.lineSeparator()
            + System.lineSeparator()
            + "This confirmation link is valid for 1 hour and can be used only once."
            + "To complete this process, you need to open the following URL in your browser window:" + System.lineSeparator()
            + " http://fitclub.azurewebsites.net/#/verification/passwordReset?token=$tokenValue";


    public void verifyEmail(User user) {
        String textBodyWithToken = TEXTBODY_EMAIL_CONFIRMATION.replace("$tokenValue", user.getVerificationToken().getEmailToken());
        textBodyWithToken = textBodyWithToken.replace("$fullName", user.getDisplayName());
        mailCompose(user, textBodyWithToken, SUBJECT_EMAIL_CONFIRMATION);
    }

    public void changeEmail(VerificationToken verificationToken, User user) {
        String textBodyWithToken = TEXTBODY_CHANGE_EMAIL.replace("$tokenValue", verificationToken.getEmailToken());
        textBodyWithToken = textBodyWithToken.replace("$fullName", user.getDisplayName());
        mailCompose(user, textBodyWithToken, SUBJECT_CHANGE_EMAIL);
    }

    public void changePassword(VerificationToken verificationToken, User user) {
        String textBodyWithToken = TEXTBODY_CHANGE_PASSWORD.replace("$tokenValue", verificationToken.getPasswordToken());
        textBodyWithToken = textBodyWithToken.replace("$fullName", user.getDisplayName());
        mailCompose(user, textBodyWithToken, SUBJECT_CHANGE_PASSWORD);
    }

    public void resetPassword(VerificationToken verificationToken, User user) {
        String textBodyWithToken = TEXTBODY_PASSWORD_RESET.replace("$tokenValue", verificationToken.getPasswordToken());
        textBodyWithToken = textBodyWithToken.replace("$fullName", user.getDisplayName());
        mailCompose(user, textBodyWithToken, SUBJECT_RESET_PASSWORD);
    }

    private void mailCompose(User user, String textBodyWithToken, String subject) {
        SimpleMailMessage mailToBeSent = new SimpleMailMessage();
        mailToBeSent.setFrom("fitclub.by.alexnec@gmail.com");
        mailToBeSent.setTo(user.getEmail());
        mailToBeSent.setText(textBodyWithToken);
        mailToBeSent.setSubject(subject);

        mailSender.send(mailToBeSent);
    }
}