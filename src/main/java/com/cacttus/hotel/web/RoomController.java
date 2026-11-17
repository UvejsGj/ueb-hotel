package com.cacttus.hotel.web;

import com.cacttus.hotel.domain.Room;
import com.cacttus.hotel.domain.RoomType;
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

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    @ModelAttribute("types")
    public RoomType[] types() {
        return RoomType.values();
    }

    @GetMapping("/app/rooms")
    public String list(Model model) {
        model.addAttribute("rooms", roomRepository.findAllByOrderByNumberAsc());
        return "rooms/list";
    }

    @GetMapping("/app/rooms/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Room room = roomRepository.findByIdWithReservations(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("room", room);
        return "rooms/detail";
    }

    @GetMapping("/admin/rooms/new")
    public String newForm(Model model) {
        model.addAttribute("room", new Room());
        return "rooms/form";
    }

    @PostMapping("/admin/rooms/new")
    public String create(@Valid @ModelAttribute("room") Room room,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        room.setId(null);
        if (roomRepository.existsByNumber(room.getNumber())) {
            result.rejectValue("number", "duplicate", "Ky numër dhome ekziston.");
        }
        if (result.hasErrors()) {
            return "rooms/form";
        }
        roomRepository.save(room);
        redirectAttributes.addFlashAttribute("message", "Dhoma u ruajt");
        return "redirect:/app/rooms";
    }

    @GetMapping("/admin/rooms/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("room", room);
        return "rooms/form";
    }

    @PostMapping("/admin/rooms/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("room") Room room,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (!roomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        room.setId(id);
        if (roomRepository.existsByNumberAndIdNot(room.getNumber(), id)) {
            result.rejectValue("number", "duplicate", "Ky numër dhome ekziston.");
        }
        if (result.hasErrors()) {
            return "rooms/form";
        }
        roomRepository.save(room);
        redirectAttributes.addFlashAttribute("message", "Dhoma u ndryshua");
        return "redirect:/app/rooms";
    }

    @PostMapping("/admin/rooms/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!roomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (reservationRepository.existsByRoomId(id)) {
            redirectAttributes.addFlashAttribute("error", "Dhoma ka rezervime dhe nuk mund të fshihet.");
            return "redirect:/app/rooms";
        }
        roomRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Dhoma u fshi");
        return "redirect:/app/rooms";
    }
}
