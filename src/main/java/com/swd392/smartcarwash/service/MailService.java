package com.swd392.smartcarwash.service;

import com.swd392.smartcarwash.exception.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public MailService(JavaMailSender mailSender,
                       @Value("${spring.mail.username}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationOtp(String to, String otp) {
        String subject = "Verify your Smart Car Wash account";
        String text = "Your verification code is: " + otp + "\nIt expires in 5 minutes.";
        sendMail(to, subject, text);
    }

    public void sendPasswordResetOtp(String to, String otp) {
        String subject = "Reset your Smart Car Wash password";
        String text = "Your password reset code is: " + otp + "\nIt expires in 5 minutes.";
        sendMail(to, subject, text);
    }

    private void sendMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (org.springframework.mail.MailAuthenticationException e) {
            log.warn("==================================================================");
            log.warn("SMTP AUTHENTICATION FAILED. Simulating success for local testing.");
            log.warn("Please check your SMTP credentials in .env if you need real emails.");
            log.warn("To: {}", to);
            log.warn("Subject: {}", subject);
            log.warn("Body:\n{}", text);
            log.warn("==================================================================");
        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new BusinessException("Failed to send email. Please try again later.");
        }
    }
}
