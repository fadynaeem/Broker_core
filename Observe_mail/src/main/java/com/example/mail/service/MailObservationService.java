package com.example.mail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
@Service
public class MailObservationService {
    private static final Logger logger = LoggerFactory.getLogger(MailObservationService.class);
    /**
     *
     * @param recipientEmail the email address of the intended recipient
     * @param subject        the subject line of the mail
     * @param body           the body content of the mail
     */
    public void recordMailObservation(String recipientEmail, String subject, String body) {
        logger.info("Mail observation recorded – recipient: {}, subject: {}", recipientEmail, subject);
        logger.debug("Mail observation body: {}", body);
    }
}
