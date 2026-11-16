package com.cacttus.hotel.repository;

import com.cacttus.hotel.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByOrderByNumberAsc();

    boolean existsByNumber(String number);

    boolean existsByNumberAndIdNot(String number, Long id);

    @Query("select r from Room r left join fetch r.reservations where r.id = :id")
    Optional<Room> findByIdWithReservations(@Param("id") Long id);
}
