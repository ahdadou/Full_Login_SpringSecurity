package com.login.demo.event.listener;

import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.login.demo.event.OnUserAccountChangeEvent;
import com.login.demo.exceptions.MailSendException;
import com.login.demo.models.User;
import com.login.demo.services.MailService;

import javax.mail.MessagingException;
import java.io.IOException;

@Component
public class OnUserAccountChangeListener implements ApplicationListener<OnUserAccountChangeEvent> {

    private final MailService mailService;

    @Autowired
    public OnUserAccountChangeListener(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * As soon as a registration event is complete, invoke the email verification
     * asynchronously in an another thread pool
     */
    @Override
    @Async
    public void onApplicationEvent(OnUserAccountChangeEvent onUserAccountChangeEvent) {
        sendAccountChangeEmail(onUserAccountChangeEvent);
    }

    /**
     * Send email verification to the user and persist the token in the database.
     */
    private void sendAccountChangeEmail(OnUserAccountChangeEvent event) {
        User user = event.getUser();
        String action = event.getAction();
        String actionStatus = event.getActionStatus();
        String recipientAddress = user.getEmail();

        try {
            mailService.sendAccountChangeEmail(action, actionStatus, recipientAddress);
        } catch (IOException | TemplateException | MessagingException e) {
            throw new MailSendException(recipientAddress, "Account Change Mail");
        }
    }
}

