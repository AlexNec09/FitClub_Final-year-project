package com.project.fitclub.shared;

import com.project.fitclub.model.User;
import com.project.fitclub.validation.verificationToken.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    final String SUBJECT_EMAIL_CONFIRMATION = "One last step to complete your registration on FitClub platform";

    final String SUBJECT_CHANGE_EMAIL = "Change your email on FitClub platform";

    final String SUBJECT_RESET_PASSWORD = "Reset your password on FitClub platform";

    final String SUBJECT_CHANGE_PASSWORD = "Change your password on FitClub platform";

    final String TEXTBODY_EMAIL_CONFIRMATION = "Please verify your email address. "
            + "Thank you for your registration. To complete this process and be able to log in,"
            + " you need to confirm your email address by opening the following URL in your browser window: "
            + "http://localhost:3000/#/verification/confirmationToken?token=$tokenValue"
            + " Thank you!";

    final String TEXTBODY_CHANGE_EMAIL = "<h1>Hello, </h1>"
            + "You're receiving this email because you requested to change your email on FitClub platform. "
            + "If you did not request this, please disregard this email. This confirmation link is valid for 1 hour and can be used only once."
            + "<br/>"
            + "If you would like to continue and change it, please click the following link:"
            + "http://localhost:3000/#/verification/changeEmail?token=$tokenValue";

    final String TEXTBODY_PASSWORD_RESET = "Hi, $firstName! You're receiving this email because you requested to reset your password on FitClub platform. "
            + "If you did not request this, please disregard this email. This confirmation link is valid for 1 hour and can be used only once."
            + "<br/>"
            + "If you would like to continue and change it, please open the following URL in your browser window:"
            + " http://localhost:3000/#/verification/passwordReset?token=$tokenValue";

    final String TEXTBODY_CHANGE_PASSWORD = "Hi, $firstName! You're receiving this email because you requested to change your password on FitClub platform. "
            + "<br/>"
            + "To complete this process, you need to open the following URL in your browser window: "
            + " http://localhost:3000/#/verification/passwordReset?token=$tokenValue";


    public void verifyEmail(User user) {
        String textBodyWithToken = TEXTBODY_EMAIL_CONFIRMATION.replace("$tokenValue", user.getVerificationToken().getEmailToken());
        mailCompose(user, textBodyWithToken, SUBJECT_EMAIL_CONFIRMATION);
    }

    public void changeEmail(VerificationToken verificationToken, User user) {
        String textBodyWithToken = TEXTBODY_CHANGE_EMAIL.replace("$tokenValue", verificationToken.getEmailToken());
        mailCompose(user, textBodyWithToken, SUBJECT_CHANGE_EMAIL);
    }

    public void changePassword(VerificationToken verificationToken, User user) {
        String textBodyWithToken = TEXTBODY_CHANGE_PASSWORD.replace("$tokenValue", verificationToken.getPasswordToken());
        textBodyWithToken = textBodyWithToken.replace("$firstName", user.getDisplayName());
        mailCompose(user, textBodyWithToken, SUBJECT_CHANGE_PASSWORD);
    }

    public void resetPassword(VerificationToken verificationToken, User user) {
        String textBodyWithToken = TEXTBODY_PASSWORD_RESET.replace("$tokenValue", verificationToken.getPasswordToken());
        textBodyWithToken = textBodyWithToken.replace("$firstName", user.getDisplayName());
        mailCompose(user, textBodyWithToken, SUBJECT_RESET_PASSWORD);
    }

    private void mailCompose(User user, String textBodyWithToken, String subject) {
        SimpleMailMessage mailToBeSent = new SimpleMailMessage();
        mailToBeSent.setFrom("fitclub.by.alexnec@gmail.com");
        mailToBeSent.setTo(user.getEmail());
        mailToBeSent.setText(textBodyWithToken);
        mailToBeSent.setSubject(subject);

        mailSender.send(mailToBeSent);

        System.out.println("Email sent to: " + user.getEmail());
    }
}
