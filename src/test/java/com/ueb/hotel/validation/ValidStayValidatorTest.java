package com.ueb.hotel.validation;

import com.ueb.hotel.model.Room;
import com.ueb.hotel.model.RoomType;
import com.ueb.hotel.repository.ReservationRepository;
import com.ueb.hotel.repository.RoomRepository;
import com.ueb.hotel.web.form.ReservationForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Teston @ValidStay përmes Hibernate Validator-it të vërtetë, me repository të simuluara. */
class ValidStayValidatorTest {

    private static final LocalDate IN = LocalDate.now().plusDays(10);
    private static final LocalDate OUT = IN.plusDays(3);

    private ReservationRepository reservations;
    private RoomRepository rooms;
    private ValidatorFactory factory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        reservations = mock(ReservationRepository.class);
        rooms = mock(RoomRepository.class);

        Room room = new Room();
        room.setId(7L);
        room.setNumber("102");
        room.setType(RoomType.DOUBLE);
        room.setCapacity(2);
        room.setPricePerNight(new BigDecimal("50.00"));
        when(rooms.findById(7L)).thenReturn(Optional.of(room));

        ConstraintValidatorFactory defaults = Validation.byDefaultProvider().configure()
            .getDefaultConstraintValidatorFactory();
        factory = Validation.byDefaultProvider().configure()
            .constraintValidatorFactory(new ConstraintValidatorFactory() {
                @Override
                @SuppressWarnings("unchecked")
                public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
                    if (key == ValidStayValidator.class) {
                        return (T) new ValidStayValidator(reservations, rooms);
                    }
                    return defaults.getInstance(key);
                }

                @Override
                public void releaseInstance(ConstraintValidator<?, ?> instance) {
                }
            })
            .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void validReservationHasNoErrors() {
        assertThat(errors(form(IN, OUT, 2))).isEmpty();
    }

    @Test
    void checkOutMustBeAfterCheckIn() {
        assertThat(errors(form(IN, IN, 1)))
            .containsEntry("checkOut", ValidStayValidator.DATES_ORDER);
        assertThat(errors(form(IN, IN.minusDays(1), 1)))
            .containsEntry("checkOut", ValidStayValidator.DATES_ORDER);
    }

    @Test
    void overlappingBookingIsRejectedOnRoomField() {
        when(reservations.existsOverlap(7L, IN, OUT, -1L)).thenReturn(true);

        assertThat(errors(form(IN, OUT, 1)))
            .containsEntry("roomId", ValidStayValidator.ROOM_TAKEN);
    }

    @Test
    void editingExcludesTheReservationItself() {
        ReservationForm f = form(IN, OUT, 1);
        f.setId(42L);
        when(reservations.existsOverlap(7L, IN, OUT, 42L)).thenReturn(false);

        assertThat(errors(f)).isEmpty();
        verify(reservations).existsOverlap(7L, IN, OUT, 42L);
    }

    @Test
    void guestsOverCapacityAreRejected() {
        assertThat(errors(form(IN, OUT, 3)))
            .containsEntry("guests", "Dhoma pranon më së shumti 2 mysafirë.");
    }

    @Test
    void reportsAllProblemsAtOnce() {
        Map<String, String> errors = errors(form(IN, IN, 5));
        assertThat(errors).containsKeys("checkOut", "guests");
    }

    @Test
    void skipsOverlapCheckWhenDatesAreInvalidOrRoomMissing() {
        errors(form(IN, IN, 1));
        ReservationForm noRoom = form(IN, OUT, 1);
        noRoom.setRoomId(null);
        errors(noRoom);

        verify(reservations, never()).existsOverlap(anyLong(), any(), any(), anyLong());
    }

    @Test
    void unknownRoomIsLeftToTheController() {
        ReservationForm f = form(IN, OUT, 1);
        f.setRoomId(99L);
        when(rooms.findById(eq(99L))).thenReturn(Optional.empty());

        assertThat(errors(f)).doesNotContainKey("roomId");
    }

    private static ReservationForm form(LocalDate in, LocalDate out, int guests) {
        ReservationForm f = new ReservationForm();
        f.setGuestName("Arta Krasniqi");
        f.setCheckIn(in);
        f.setCheckOut(out);
        f.setGuests(guests);
        f.setRoomId(7L);
        return f;
    }

    /** fusha → mesazhi, vetëm për gabimet e @ValidStay. */
    private Map<String, String> errors(ReservationForm f) {
        Set<ConstraintViolation<ReservationForm>> violations = validator.validate(f);
        return violations.stream()
            .filter(v -> v.getConstraintDescriptor().getAnnotation() instanceof ValidStay)
            .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage));
    }
}
