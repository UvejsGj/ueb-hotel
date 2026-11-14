package com.cacttus.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Shkruani emrin e mysafirit")
    @Size(max = 100, message = "Emri ka më së shumti 100 shenja")
    private String guestName;

    @NotBlank(message = "Shkruani e-mail-in e mysafirit")
    @Email(message = "E-mail-i nuk është i saktë")
    private String guestEmail;

    @NotNull(message = "Zgjidhni datën e hyrjes")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkIn;

    @NotNull(message = "Zgjidhni datën e daljes")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOut;

    @NotNull(message = "Shkruani numrin e mysafirëve")
    @Min(value = 1, message = "Duhet së paku 1 mysafir")
    @Max(value = 6, message = "Më së shumti 6 mysafirë")
    private Integer guests;

    @NotNull(message = "Zgjidhni dhomën")
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(updatable = false)
    private String createdBy;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @AssertTrue(message = "Data e daljes duhet të jetë pas datës së hyrjes")
    public boolean isCheckOutAfterCheckIn() {
        if (checkIn == null || checkOut == null) {
            return true;
        }
        return checkOut.isAfter(checkIn);
    }
}
