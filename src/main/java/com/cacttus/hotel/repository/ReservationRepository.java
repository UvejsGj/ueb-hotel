package com.cacttus.hotel.repository;

import com.cacttus.hotel.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByCheckInAsc();

    boolean existsByRoomId(Long roomId);
}
