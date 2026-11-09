package com.ueb.hotel.repository;

import com.ueb.hotel.model.Reservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = "room")
    List<Reservation> findAllByOrderByCheckInAscIdAsc();

    @EntityGraph(attributePaths = "room")
    List<Reservation> findByRoomIdOrderByCheckInAsc(Long roomId);

    boolean existsByRoomId(Long roomId);
}
