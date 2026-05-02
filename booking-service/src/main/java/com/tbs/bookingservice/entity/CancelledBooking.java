package com.tbs.bookingservice.entity;

import com.tbs.bookingservice.entity.enums.BookingCancellationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="cancelled_booking")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter

//For the tickets which are cancelled will be got from the feign client.
public class CancelledBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cancellation_reason;

    @Enumerated(EnumType.STRING)
    private BookingCancellationType bookingCancellationType;

    @CreationTimestamp
    private LocalDateTime cancelled_booking_date;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
}
