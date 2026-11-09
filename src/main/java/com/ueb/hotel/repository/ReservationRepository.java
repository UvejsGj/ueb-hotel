package com.ueb.hotel.repository;

import com.ueb.hotel.model.Reservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = "room")
    List<Reservation> findAllByOrderByCheckInAscIdAsc();

    @EntityGraph(attributePaths = "room")
    List<Reservation> findByRoomIdOrderByCheckInAsc(Long roomId);

    boolean existsByRoomId(Long roomId);

    /**
     * A ka rezervim tjetër për këtë dhomë që mbivendoset me [checkIn, checkOut)?
     * {@code excludeId} është rezervimi që po ndryshohet (-1 për rezervim të ri).
     */
    @Query("""
        select count(r) > 0 from Reservation r
        where r.room.id = :roomId
          and r.id <> :excludeId
          and r.checkIn < :checkOut
          and r.checkOut > :checkIn
        """)
    boolean existsOverlap(@Param("roomId") Long roomId,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut,
                          @Param("excludeId") Long excludeId);

    /** Id-të e dhomave që janë të zëna (pjesërisht) në [from, to). */
    @Query("""
        select distinct r.room.id from Reservation r
        where r.id <> :excludeId
          and r.checkIn < :to
          and r.checkOut > :from
        """)
    List<Long> findBookedRoomIds(@Param("from") LocalDate from,
                                 @Param("to") LocalDate to,
                                 @Param("excludeId") Long excludeId);
}
