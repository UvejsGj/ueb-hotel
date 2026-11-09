package com.ueb.hotel.web.form;

import com.ueb.hotel.model.Reservation;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class ReservationForm {

    @NotBlank(message = "Emri i mysafirit është i detyrueshëm.")
    @Size(max = 80, message = "Emri mund të ketë më së shumti 80 shkronja.")
    private String guestName;

    @Size(max = 20, message = "Telefoni mund të ketë më së shumti 20 shenja.")
    private String guestPhone;

    @NotNull(message = "Zgjidhni datën e ardhjes.")
    @FutureOrPresent(message = "Data e ardhjes nuk mund të jetë në të kaluarën.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkIn;

    @NotNull(message = "Zgjidhni datën e largimit.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOut;

    @NotNull(message = "Shkruani numrin e mysafirëve.")
    @Min(value = 1, message = "Duhet të paktën 1 mysafir.")
    private Integer guests;

    @NotNull(message = "Zgjidhni dhomën.")
    private Long roomId;

    public static ReservationForm from(Reservation r) {
        ReservationForm f = new ReservationForm();
        f.guestName = r.getGuestName();
        f.guestPhone = r.getGuestPhone();
        f.checkIn = r.getCheckIn();
        f.checkOut = r.getCheckOut();
        f.guests = r.getGuests();
        f.roomId = r.getRoom().getId();
        return f;
    }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }

    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }

    public Integer getGuests() { return guests; }
    public void setGuests(Integer guests) { this.guests = guests; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
}
