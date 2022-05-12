package com.project.fitclub.shared;

import com.project.fitclub.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    @Autowired
    private JavaMailSender mailSender;


    // The subject line for the email.
    final String SUBJECT = "One last step to complete your registration with Hoaxify App";

    final String PASSWORD_RESET_SUBJECT = "Password reset request";

    // verification email messages
    // The HTML body for the email.
//    final String HTMLBODY = "<h1>Please verify your email address</h1>"
//            + "<p>Thank you for registering with our Hoaxify App. To complete registration process and be able to log in,"
//            + " click on the following link: "
//            + "<a href='http://localhost:3000/#/verification/confirmationToken?token=$tokenValue'>"
//
//            + "Final step to complete your registration" + "</a><br/><br/>"
//            + "Thank you! And we are waiting for you inside!";

    // The email body for recipients with non-HTML email clients.
    final String TEXTBODY = "Please verify your email address. "
            + "Thank you for your registration. To complete this process and be able to log in,"
            + " you need to confirm your email address by opening thefollowing URL in your browser window: "
            + "http://localhost:3000/#/verification/confirmationToken?token=$tokenValue>"
            + " Thank you!";

    // password reset messages
    final String PASSWORD_RESET_HTMLBODY = "<h1>A request to reset your password</h1>"
            + "<p>Hi, $firstName!</p> "
            + "<p>Someone has requested to reset your password with our project. If it were not you, please ignore it."
            + " otherwise please click on the link below to set a new password: "
            + "<a href='http://localhost:3000/verification_service_war/password-reset.html?token=$tokenValue'>"
            + " Click this link to Reset Password"
            + "</a><br/><br/>"
            + "Thank you!";

    // The email body for recipients with non-HTML email clients.
    final String PASSWORD_RESET_TEXTBODY = "A request to reset your password "
            + "Hi, $firstName! "
            + "Someone has requested to reset your password with our project. If it were not you, please ignore it."
            + " otherwise please open the link below in your browser window to set a new password: "
            + " http://localhost:3000/verification_service_war/password-reset.html?token=$tokenValue"
            + " Thank you!";


    public void verifyEmail(User user) {

//        String htmlBodyWithToken = HTMLBODY.replace("$tokenValue", user.getEmailVerificationToken());
        String textBodyWithToken = TEXTBODY.replace("$tokenValue", user.getEmailVerificationToken());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("fitclub.by.alexnec@gmail.com");
        message.setTo(user.getUsername());
        message.setText(textBodyWithToken);
        message.setSubject(SUBJECT);

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
