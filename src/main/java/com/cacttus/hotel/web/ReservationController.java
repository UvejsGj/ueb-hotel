package com.cacttus.hotel.web;

import com.cacttus.hotel.domain.Reservation;
import com.cacttus.hotel.domain.Room;
import com.cacttus.hotel.repository.ReservationRepository;
import com.cacttus.hotel.repository.RoomRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    // Lista e dhomave për <select>-in në formë. Shtohet në model për çdo request të këtij controller-i.
    @ModelAttribute("rooms")
    public List<Room> rooms() {
        return roomRepository.findAllByOrderByNumberAsc();
    }

    @GetMapping("/app/reservations")
    public String list(Model model) {
        model.addAttribute("reservations", reservationRepository.findAllByOrderByCheckInAsc());
        return "reservations/list";
    }

    @GetMapping("/app/reservations/new")
    public String newForm(Model model) {
        model.addAttribute("reservation", new Reservation());
        return "reservations/form";
    }

    @PostMapping("/app/reservations/new")
    public String create(@Valid @ModelAttribute("reservation") Reservation reservation,
                         BindingResult result,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "reservations/form";
        }
        // Login-i vjen më vonë (feature/login); deri atëherë principal është null.
        if (principal != null) {
            reservation.setCreatedBy(principal.getName());
        } else {
            reservation.setCreatedBy("system");
        }
        reservationRepository.save(reservation);
        redirectAttributes.addFlashAttribute("message", "Rezervimi u ruajt");
        return "redirect:/app/reservations";
    }

    @GetMapping("/app/reservations/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("reservation", reservation);
        return "reservations/form";
    }

    @PostMapping("/app/reservations/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("reservation") Reservation reservation,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (!reservationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        // id-ja vjen nga URL-ja, jo nga forma.
        reservation.setId(id);
        if (result.hasErrors()) {
            return "reservations/form";
        }
        // createdBy dhe createdAt kanë @Column(updatable = false), prandaj mbesin si në databazë.
        reservationRepository.save(reservation);
        redirectAttributes.addFlashAttribute("message", "Rezervimi u ndryshua");
        return "redirect:/app/reservations";
    }

    @PostMapping("/admin/reservations/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!reservationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        reservationRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Rezervimi u fshi");
        return "redirect:/app/reservations";
    }
}
