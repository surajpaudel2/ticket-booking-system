package com.tbs.bookingservice.scheduler;

import com.tbs.bookingservice.entity.Booking;
import com.tbs.bookingservice.entity.enums.BookingStatus;
import com.tbs.bookingservice.entity.enums.OutboxEventType;
import com.tbs.bookingservice.mapper.SchedulerMapper;
import com.tbs.bookingservice.repository.BookingRepository;
import com.tbs.bookingservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingReminderScheduler {

    private final BookingRepository bookingRepository;
    private final OutboxService outboxService;
    private final SchedulerMapper schedulerMapper;

    @Scheduled(fixedDelay = 60000)
    public void processReminder1() {
        LocalDateTime now = LocalDateTime.now();
        bookingRepository
                .findAllByStatusAndExpiresAtAfterAndReminder1SentAtIsNull(BookingStatus.PENDING, now)
                .stream()
                .filter(b -> b.getExpiresAt().isBefore(now.plusMinutes(10)))
                .forEach(this::sendReminder1);
    }

    @Scheduled(fixedDelay = 60000)
    public void processReminder2() {
        LocalDateTime now = LocalDateTime.now();
        bookingRepository
                .findAllByStatusAndExpiresAtAfterAndReminder1SentAtIsNotNullAndReminder2SentAtIsNull(BookingStatus.PENDING, now)
                .stream()
                .filter(b -> b.getExpiresAt().isBefore(now.plusMinutes(5)))
                .forEach(this::sendReminder2);
    }

    private void sendReminder1(Booking booking) {
        outboxService.saveEvent(OutboxEventType.BOOKING_REMINDER_1,
                schedulerMapper.toReminderPayload(booking, 10),
                booking.getId(), booking.getBookingAttemptId());
        booking.setReminder1SentAt(LocalDateTime.now());
        bookingRepository.save(booking);
        log.info("Reminder1 outbox event saved for bookingId={}", booking.getId());
    }

    private void sendReminder2(Booking booking) {
        outboxService.saveEvent(OutboxEventType.BOOKING_REMINDER_2,
                schedulerMapper.toReminderPayload(booking, 5),
                booking.getId(), booking.getBookingAttemptId());
        booking.setReminder2SentAt(LocalDateTime.now());
        bookingRepository.save(booking);
        log.info("Reminder2 outbox event saved for bookingId={}", booking.getId());
    }
}