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


    // The subject line for the email.
    final String SUBJECT_EMAIL_CONFIRMATION = "One last step to complete your registration on FitClub platform";

    final String SUBJECT_CHANGE_EMAIL = "Change your email on FitClub platform";


    final String PASSWORD_RESET_SUBJECT = "Password reset request";

    // The email body for recipients with non-HTML email clients.
    final String TEXTBODY_EMAIL_CONFIRMATION = "Please verify your email address. "
            + "Thank you for your registration. To complete this process and be able to log in,"
            + " you need to confirm your email address by opening thefollowing URL in your browser window: "
            + "http://localhost:3000/#/verification/confirmationToken?token=$tokenValue"
            + " Thank you!";

    final String TEXTBODY_CHANGE_EMAIL = "<h1>Hello, </h1>"
            + "You're receiving this email because you requested to change your email on FitClub platform. "
            + "If you did not request this, please disregard this email. This confirmation link is valid for 1 hour and can be used only once."
            + "<br/>"
            + "If you would like to continue and change it, please click the following link:"
            + "http://localhost:3000/#/verification/changeEmail?token=$tokenValue"
            + " Click here to redirect for change your email.</a>";

    // The email body for recipients with non-HTML email clients.
    final String PASSWORD_RESET_TEXTBODY = "A request to reset your password "
            + "Hi, $firstName! "
            + "Someone has requested to reset your password with our project. If it were not you, please ignore it."
            + " otherwise please open the link below in your browser window to set a new password: "
            + " http://localhost:3000/verification_service_war/password-reset.html?token=$tokenValue"
            + " Thank you!";


    public void verifyEmail(User user) {

//        String htmlBodyWithToken = HTMLBODY.replace("$tokenValue", user.getEmailVerificationToken());
        String textBodyWithToken = TEXTBODY_EMAIL_CONFIRMATION.replace("$tokenValue", user.getVerificationToken().getEmailToken());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("fitclub.by.alexnec@gmail.com");
        message.setTo(user.getUsername());
        message.setText(textBodyWithToken);
        message.setSubject(SUBJECT_EMAIL_CONFIRMATION);

        mailSender.send(message);

        System.out.println("Email sent to: " + user.getUsername());
    }

    public void changeEmail(VerificationToken verificationToken, User user) {

        String textBodyWithToken = TEXTBODY_CHANGE_EMAIL.replace("$tokenValue", verificationToken.getEmailToken());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("fitclub.by.alexnec@gmail.com");
        message.setTo(user.getUsername());
        message.setText(textBodyWithToken);
        message.setSubject(SUBJECT_CHANGE_EMAIL);

        mailSender.send(message);

        System.out.println("Email sent to: " + user.getUsername());
    }


//    public boolean sendPasswordResetRequest(String firstName, String email, String token) {
//        boolean returnValue = false;
//
//        String htmlBodyWithToken = PASSWORD_RESET_HTMLBODY.replace("$tokenValue", token);
//        htmlBodyWithToken = htmlBodyWithToken.replace("$firstName", firstName);
//
//        String textBodyWithToken = PASSWORD_RESET_TEXTBODY.replace("$tokenValue", token);
//        textBodyWithToken = textBodyWithToken.replace("$firstName", firstName);
//
//        SendEmailRequest request = new SendEmailRequest()
//                // set destination
//                .withDestination(new Destination().withToAddresses(email))
//                // set messages
//                .withMessage(new Message()
//                        .withBody(new Body()
//                                .withHtml(new Content()
//                                        .withCharset("UTF-8").withData(htmlBodyWithToken))
//                                .withText(new Content()
//                                        .withCharset("UTF-8").withData(textBodyWithToken)))
//                        .withSubject(new Content()
//                                .withCharset("UTF-8").withData(PASSWORD_RESET_SUBJECT)))
//                // set from
//                .withSource(FROM);
//
//        SendEmailResult result = client.sendEmail(request);
//        if (result != null && (result.getMessageId() != null && !result.getMessageId().isEmpty())) {
//            returnValue = true;
//        }
//
//        System.out.println("Email sent to: " + email);
//        return returnValue;
//    }
}
