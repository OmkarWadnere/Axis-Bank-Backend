package com.axis.bank.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class EmailService {

    private JavaMailSender mailSender;

    @Async
    public void sendMail(String receiverMailId, String mailBody, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(receiverMailId);
        message.setSubject(subject);
        message.setText(mailBody);
        message.setFrom("wadnereomkar@gmail.com");
        mailSender.send(message);
        log.info("Mail send successfully for : {}", subject);
    }
}
