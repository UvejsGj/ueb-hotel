package com.ueb.hotel.validation;

import com.ueb.hotel.model.Room;
import com.ueb.hotel.repository.ReservationRepository;
import com.ueb.hotel.repository.RoomRepository;
import com.ueb.hotel.web.form.ReservationForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidStayValidator implements ConstraintValidator<ValidStay, ReservationForm> {

    static final String DATES_ORDER = "Data e largimit duhet të jetë pas datës së ardhjes.";
    static final String ROOM_TAKEN = "Dhoma është e zënë në këto data.";
    static final String CAPACITY = "Dhoma pranon më së shumti %d mysafirë.";

    private final ReservationRepository reservations;
    private final RoomRepository rooms;

    /** Spring i injekton repository-t (LocalValidatorFactoryBean). */
    public ValidStayValidator(ReservationRepository reservations, RoomRepository rooms) {
        this.reservations = reservations;
        this.rooms = rooms;
    }

    @Override
    public boolean isValid(ReservationForm form, ConstraintValidatorContext ctx) {
        if (form == null) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();
        boolean valid = true;

        boolean datesPresent = form.getCheckIn() != null && form.getCheckOut() != null;
        boolean datesOrdered = datesPresent && form.getCheckOut().isAfter(form.getCheckIn());
        if (datesPresent && !datesOrdered) {
            add(ctx, "checkOut", DATES_ORDER);
            valid = false;
        }

        if (form.getRoomId() == null) {
            return valid; // @NotNull te roomId e raporton këtë
        }
        Room room = rooms.findById(form.getRoomId()).orElse(null);
        if (room == null) {
            return valid; // controller-i e raporton dhomën që mungon
        }

        if (datesOrdered) {
            long exclude = form.getId() == null ? -1L : form.getId();
            if (reservations.existsOverlap(room.getId(), form.getCheckIn(), form.getCheckOut(), exclude)) {
                add(ctx, "roomId", ROOM_TAKEN);
                valid = false;
            }
        }

        if (form.getGuests() != null && room.getCapacity() != null && form.getGuests() > room.getCapacity()) {
            add(ctx, "guests", CAPACITY.formatted(room.getCapacity()));
            valid = false;
        }
        return valid;
    }

    private static void add(ConstraintValidatorContext ctx, String field, String message) {
        ctx.buildConstraintViolationWithTemplate(escape(message))
            .addPropertyNode(field)
            .addConstraintViolation();
    }

    /** Mesazhi kalon si template; i shmangim kllapat që të mos interpretohen. */
    private static String escape(String message) {
        return message.replace("\\", "\\\\").replace("{", "\\{").replace("}", "\\}").replace("$", "\\$");
    }
}
