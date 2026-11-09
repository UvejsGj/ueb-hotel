package com.ueb.hotel.web;

import com.ueb.hotel.model.Reservation;
import com.ueb.hotel.model.Room;
import com.ueb.hotel.repository.ReservationRepository;
import com.ueb.hotel.repository.RoomRepository;
import com.ueb.hotel.web.form.ReservationForm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ReservationController {

    private final ReservationRepository reservations;
    private final RoomRepository rooms;
    private final LocalValidatorFactoryBean validator;

    public ReservationController(ReservationRepository reservations, RoomRepository rooms,
                                 LocalValidatorFactoryBean validator) {
        this.reservations = reservations;
        this.rooms = rooms;
        this.validator = validator;
    }

    /** Id-ja nuk merret kurrë nga forma (do ta anashkalonte kontrollin e mbivendosjes). */
    @InitBinder("form")
    void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("id");
    }

    @GetMapping("/app/reservations")
    public String list(Model model) {
        model.addAttribute("reservations", reservations.findAllByOrderByCheckInAscIdAsc());
        return "reservations/list";
    }

    @GetMapping("/app/reservations/new")
    public String newForm(Model model) {
        return showForm(model, new ReservationForm(), null);
    }

    @PostMapping("/app/reservations/new")
    public String create(@Valid @ModelAttribute("form") ReservationForm form, BindingResult result,
                         Model model, Principal principal, RedirectAttributes redirect) {
        Room room = findRoom(form, result);
        if (result.hasErrors()) {
            return showForm(model, form, null);
        }
        Reservation r = new Reservation();
        apply(form, room, r);
        r.setCreatedBy(principal.getName());
        r.setCreatedAt(LocalDateTime.now());
        reservations.save(r);
        redirect.addFlashAttribute("success", "Rezervimi për " + r.getGuestName() + " u ruajt.");
        return "redirect:/app/reservations";
    }

    @GetMapping("/app/reservations/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        return showForm(model, ReservationForm.from(find(id)), id);
    }

    @PostMapping("/app/reservations/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute("form") ReservationForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        Reservation r = find(id);
        form.setId(id);
        validator.validate(form, result);
        Room room = findRoom(form, result);
        if (result.hasErrors()) {
            return showForm(model, form, id);
        }
        apply(form, room, r);
        reservations.save(r);
        redirect.addFlashAttribute("success", "Rezervimi për " + r.getGuestName() + " u ndryshua.");
        return "redirect:/app/reservations";
    }

    @PostMapping("/admin/reservations/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        Reservation r = find(id);
        reservations.delete(r);
        redirect.addFlashAttribute("success", "Rezervimi për " + r.getGuestName() + " u fshi.");
        return "redirect:/app/reservations";
    }

    private String showForm(Model model, ReservationForm form, Long id) {
        model.addAttribute("form", form);
        model.addAttribute("reservationId", id);
        model.addAttribute("rooms", rooms.findAllByOrderByNumberAsc());
        return "reservations/form";
    }

    private Room findRoom(ReservationForm form, BindingResult result) {
        if (form.getRoomId() == null) {
            return null;
        }
        Room room = rooms.findById(form.getRoomId()).orElse(null);
        if (room == null) {
            result.rejectValue("roomId", "room.missing", "Dhoma e zgjedhur nuk ekziston.");
        }
        return room;
    }

    private static void apply(ReservationForm form, Room room, Reservation r) {
        r.setGuestName(form.getGuestName().trim());
        String phone = form.getGuestPhone();
        r.setGuestPhone(phone == null || phone.isBlank() ? null : phone.trim());
        r.setCheckIn(form.getCheckIn());
        r.setCheckOut(form.getCheckOut());
        r.setGuests(form.getGuests());
        r.setRoom(room);
    }

    private Reservation find(Long id) {
        return reservations.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
