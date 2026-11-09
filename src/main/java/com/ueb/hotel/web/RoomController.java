package com.ueb.hotel.web;

import com.ueb.hotel.model.Room;
import com.ueb.hotel.model.RoomType;
import com.ueb.hotel.repository.ReservationRepository;
import com.ueb.hotel.repository.RoomRepository;
import com.ueb.hotel.web.form.RoomForm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RoomController {

    private final RoomRepository rooms;
    private final ReservationRepository reservations;

    public RoomController(RoomRepository rooms, ReservationRepository reservations) {
        this.rooms = rooms;
        this.reservations = reservations;
    }

    /** Detajet e dhomës me rezervimet e saj: e sheh edhe recepsionisti. */
    @GetMapping("/app/rooms/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("room", find(id));
        model.addAttribute("reservations", reservations.findByRoomIdOrderByCheckInAsc(id));
        return "rooms/detail";
    }

    @GetMapping("/admin/rooms")
    public String list(Model model) {
        model.addAttribute("rooms", rooms.findAllByOrderByNumberAsc());
        return "rooms/list";
    }

    @GetMapping("/admin/rooms/new")
    public String newForm(Model model) {
        return showForm(model, new RoomForm(), null);
    }

    @PostMapping("/admin/rooms/new")
    public String create(@Valid @ModelAttribute("form") RoomForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (form.getNumber() != null && rooms.existsByNumberIgnoreCase(form.getNumber().trim())) {
            result.rejectValue("number", "room.duplicate", "Ky numër dhome ekziston tashmë.");
        }
        if (result.hasErrors()) {
            return showForm(model, form, null);
        }
        Room room = new Room();
        form.applyTo(room);
        rooms.save(room);
        redirect.addFlashAttribute("success", "Dhoma " + room.getNumber() + " u shtua.");
        return "redirect:/admin/rooms";
    }

    @GetMapping("/admin/rooms/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        return showForm(model, RoomForm.from(find(id)), id);
    }

    @PostMapping("/admin/rooms/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") RoomForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        Room room = find(id);
        if (form.getNumber() != null && rooms.existsByNumberIgnoreCaseAndIdNot(form.getNumber().trim(), id)) {
            result.rejectValue("number", "room.duplicate", "Ky numër dhome ekziston tashmë.");
        }
        if (result.hasErrors()) {
            return showForm(model, form, id);
        }
        form.applyTo(room);
        rooms.save(room);
        redirect.addFlashAttribute("success", "Dhoma " + room.getNumber() + " u ndryshua.");
        return "redirect:/admin/rooms";
    }

    /** Faqja e konfirmimit: rooms/form.html në modalitetin "fshi" (vetëm lexim). */
    @GetMapping("/admin/rooms/{id}/delete")
    public String confirmDelete(@PathVariable Long id, Model model) {
        showForm(model, RoomForm.from(find(id)), id);
        model.addAttribute("deleteMode", true);
        model.addAttribute("hasReservations", reservations.existsByRoomId(id));
        return "rooms/form";
    }

    @PostMapping("/admin/rooms/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        Room room = find(id);
        if (reservations.existsByRoomId(id)) {
            redirect.addFlashAttribute("error", "Dhoma ka rezervime dhe nuk mund të fshihet.");
        } else {
            rooms.delete(room);
            redirect.addFlashAttribute("success", "Dhoma " + room.getNumber() + " u fshi.");
        }
        return "redirect:/admin/rooms";
    }

    private String showForm(Model model, RoomForm form, Long id) {
        model.addAttribute("form", form);
        model.addAttribute("roomId", id);
        model.addAttribute("roomTypes", RoomType.values());
        return "rooms/form";
    }

    private Room find(Long id) {
        return rooms.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
