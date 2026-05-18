package com.erbol.ems.admin;

import com.erbol.ems.category.CategoryRepository;
import com.erbol.ems.event.EventRepository;
import com.erbol.ems.event.EventStatus;
import com.erbol.ems.ticket.TicketRepository;
import com.erbol.ems.ticket.TicketStatus;
import com.erbol.ems.user.User;
import com.erbol.ems.user.UserRepository;
import com.erbol.ems.user.UserType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;

    public AdminController(UserRepository userRepository,
                           EventRepository eventRepository,
                           TicketRepository ticketRepository,
                           CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalEvents", eventRepository.count());
        stats.put("publishedEvents",
                (long) eventRepository.findAllByStatusOrderByStartAtAsc(
                        EventStatus.PUBLISHED,
                        org.springframework.data.domain.Pageable.unpaged()
                ).getNumberOfElements());
        stats.put("totalCategories", categoryRepository.count());
        stats.put("activeTickets", ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.ACTIVE)
                .count());

        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users/list";
    }

    @PostMapping("/users/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes ra) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.erbol.ems.common.exception.ResourceNotFoundException("User", id));

        if (user.getUserType() == UserType.ADMIN) {
            ra.addFlashAttribute("errorMessage", "Cannot deactivate administrators.");
            return "redirect:/admin/users";
        }

        if (user.isActive()) {
            user.deactivate();
            ra.addFlashAttribute("successMessage", "User '" + user.getEmail() + "' deactivated.");
        } else {
            user.activate();
            ra.addFlashAttribute("successMessage", "User '" + user.getEmail() + "' activated.");
        }
        userRepository.save(user);
        return "redirect:/admin/users";
    }
}