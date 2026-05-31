package com.tbs.notificationservice.service.impl;

import com.tbs.notificationservice.client.dto.response.FixtureResponse;
import com.tbs.notificationservice.client.dto.response.UserResponse;
import com.tbs.notificationservice.messaging.payload.inbound.BookingAttemptNudgeEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingConfirmedPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingExpiredEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingFailedEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingPaymentInitiatedEventPayload;
import com.tbs.notificationservice.messaging.payload.inbound.BookingReminderEventPayload;
import com.tbs.notificationservice.service.EmailService;
import com.tbs.notificationservice.mapper.EmailContextMapper;
import com.tbs.notificationservice.service.email.EmailTemplate;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender     mailSender;
    private final TemplateEngine     templateEngine;
    private final MessageSource      messageSource;
    private final EmailContextMapper contextMapper;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendBookingExpiredEmail(BookingExpiredEventPayload payload) {
        if (guardNull(payload.recipientEmail(), "booking-expired", payload.userId())) return;
        dispatch(payload.recipientEmail(), EmailTemplate.BOOKING_EXPIRED,
                contextMapper.toBookingExpiredContext(payload));
    }

    @Override
    public void sendNudgeEmail(BookingAttemptNudgeEventPayload payload,
                               UserResponse user, FixtureResponse fixture) {
        if (guardNull(user.email(), "nudge", payload.userId())) return;
        dispatch(user.email(), EmailTemplate.BOOKING_NUDGE,
                contextMapper.toNudgeContext(payload, user, fixture));
    }

    @Override
    public void sendReminder1Email(BookingReminderEventPayload payload) {
        if (guardNull(payload.recipientEmail(), "reminder-1", payload.userId())) return;
        dispatch(payload.recipientEmail(), EmailTemplate.BOOKING_REMINDER_1,
                contextMapper.toReminderContext(payload));
    }

    @Override
    public void sendReminder2Email(BookingReminderEventPayload payload) {
        if (guardNull(payload.recipientEmail(), "reminder-2", payload.userId())) return;
        dispatch(payload.recipientEmail(), EmailTemplate.BOOKING_REMINDER_2,
                contextMapper.toReminderContext(payload));
    }

    @Override
    public void sendBookingFailedEmail(BookingFailedEventPayload payload) {
        if (guardNull(payload.recipientEmail(), "booking-failed", payload.userId())) return;
        dispatch(payload.recipientEmail(), EmailTemplate.BOOKING_FAILED,
                contextMapper.toBookingFailedContext(payload));
    }

    @Override
    public void sendPaymentInitiatedEmail(BookingPaymentInitiatedEventPayload payload) {
        if (guardNull(payload.recipientEmail(), "payment-initiated", payload.userId())) return;
        dispatch(payload.recipientEmail(), EmailTemplate.PAYMENT_INITIATED,
                contextMapper.toPaymentInitiatedContext(payload));
    }

    @Override
    public void sendBookingConfirmedEmail(BookingConfirmedPayload payload) {
        if (guardNull(payload.recipientEmail(), "booking-confirmed", payload.userId())) return;
        dispatch(payload.recipientEmail(), EmailTemplate.BOOKING_CONFIRMED,
                contextMapper.toBookingConfirmedContext(payload));
    }

    // ── private plumbing ────────────────────────────────────────────────────

    private boolean guardNull(String email, String emailType, Object userId) {
        if (email == null) {
            log.warn("Skipping {} email — email is null userId={}", emailType, userId);
            return true;
        }
        return false;
    }

    private void dispatch(String to, EmailTemplate template, Context ctx) {
        String subject = messageSource.getMessage(
                template.subjectKey, null, Locale.getDefault());
        buildAndSend(to, subject, template.templateName, ctx);
    }

    private void buildAndSend(String to, String subject, String templateName, Context ctx) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            log.debug("Processing template={} for to={}", templateName, to);
            String html = templateEngine.process(templateName, ctx);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to={} subject={}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to={} template={}", to, templateName, e);
        }
    }
}