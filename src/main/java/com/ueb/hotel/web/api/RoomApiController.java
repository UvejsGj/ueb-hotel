package com.ueb.hotel.web.api;

import com.ueb.hotel.model.Room;
import com.ueb.hotel.repository.ReservationRepository;
import com.ueb.hotel.repository.RoomRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/rooms")
public class RoomApiController {

    private final RoomRepository rooms;
    private final ReservationRepository reservations;

    public RoomApiController(RoomRepository rooms, ReservationRepository reservations) {
        this.rooms = rooms;
        this.reservations = reservations;
    }

    /**
     * Dhomat e lira në [from, to). Me {@code exclude} (id e rezervimit që po ndryshohet)
     * dhoma e atij rezervimi nuk llogaritet e zënë nga vetë ai.
     */
    @GetMapping("/available")
    public List<RoomDto> available(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long exclude) {
        if (!to.isAfter(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Data e largimit duhet të jetë pas datës së ardhjes.");
        }
        Set<Long> booked = new HashSet<>(
            reservations.findBookedRoomIds(from, to, exclude == null ? -1L : exclude));
        return rooms.findAllByOrderByNumberAsc().stream()
            .filter(r -> !booked.contains(r.getId()))
            .map(RoomDto::of)
            .toList();
    }

    public record RoomDto(Long id, String number, String type, String typeLabel,
                          Integer capacity, BigDecimal pricePerNight) {
        static RoomDto of(Room r) {
            return new RoomDto(r.getId(), r.getNumber(), r.getType().name(), r.getType().getLabel(),
                r.getCapacity(), r.getPricePerNight());
        }
    }
}
