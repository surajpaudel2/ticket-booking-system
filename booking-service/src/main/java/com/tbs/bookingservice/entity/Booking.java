package com.tbs.bookingservice.entity;

import com.tbs.bookingservice.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

//Note for this class if we need the ticket related details then we will be using the feign client helps to get the things about  ticket like total tickets, ticket details and so on.
//We will find whether the user is a season ticket holder or the normal ticket holder by using the user id.
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
