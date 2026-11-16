package com.cacttus.hotel;

import com.cacttus.hotel.domain.Reservation;
import com.cacttus.hotel.domain.Room;
import com.cacttus.hotel.domain.RoomType;
import com.cacttus.hotel.repository.ReservationRepository;
import com.cacttus.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SeedData implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public void run(String... args) {
        if (roomRepository.count() > 0) {
            return;
        }

        Room r101 = room("101", RoomType.SINGLE, 1, 1, "30.00");
        Room r102 = room("102", RoomType.DOUBLE, 1, 2, "45.00");
        Room r201 = room("201", RoomType.SINGLE, 2, 1, "32.00");
        Room r202 = room("202", RoomType.DOUBLE, 2, 2, "48.00");
        Room r203 = room("203", RoomType.DOUBLE, 2, 3, "55.00");
        Room r301 = room("301", RoomType.SUITE, 3, 4, "95.00");
        roomRepository.saveAll(List.of(r101, r102, r201, r202, r203, r301));

        reservationRepository.saveAll(List.of(
                reservation("Arben Hoxha", "arben.hoxha@example.com", "2026-12-03", "2026-12-06", 1, r101),
                reservation("Elira Krasniqi", "elira.krasniqi@example.com", "2026-12-10", "2026-12-14", 2, r102),
                reservation("Driton Berisha", "driton.berisha@example.com", "2026-12-18", "2026-12-21", 3, r203),
                reservation("Blerta Gashi", "blerta.gashi@example.com", "2026-12-27", "2026-12-31", 4, r301)
        ));
    }

    private Room room(String number, RoomType type, int floor, int capacity, String price) {
        Room room = new Room();
        room.setNumber(number);
        room.setType(type);
        room.setFloor(floor);
        room.setCapacity(capacity);
        room.setPricePerNight(new BigDecimal(price));
        return room;
    }

    private Reservation reservation(String guestName, String guestEmail, String checkIn, String checkOut,
                                    int guests, Room room) {
        Reservation reservation = new Reservation();
        reservation.setGuestName(guestName);
        reservation.setGuestEmail(guestEmail);
        reservation.setCheckIn(LocalDate.parse(checkIn));
        reservation.setCheckOut(LocalDate.parse(checkOut));
        reservation.setGuests(guests);
        reservation.setRoom(room);
        reservation.setCreatedBy("system");
        return reservation;
    }
}
