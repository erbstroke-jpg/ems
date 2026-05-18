package com.erbol.ems.ticket;

import com.erbol.ems.common.exception.BusinessRuleException;
import com.erbol.ems.common.exception.EventFullException;
import com.erbol.ems.user.Attendee;
import com.erbol.ems.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('ATTENDEE')")
public class TicketController {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    public TicketController(TicketService ticketService,
                            UserRepository userRepository) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    private Attendee currentAttendee(UserDetails principal) {
        return userRepository.findAttendeeByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user is not an attendee: " + principal.getUsername()));
    }

    @PostMapping("/events/{eventId}/register")
    public String register(@AuthenticationPrincipal UserDetails principal,
                           @PathVariable Long eventId,
                           RedirectAttributes ra) {
        Attendee attendee = currentAttendee(principal);
        try {
            Ticket ticket = ticketService.register(attendee, eventId);
            ra.addFlashAttribute("successMessage",
                    "Registered! Your ticket code: " + ticket.getCode());
            return "redirect:/my-tickets";
        } catch (EventFullException ex) {
            ra.addFlashAttribute("errorMessage", "Sorry, this event is sold out.");
            return "redirect:/events/" + eventId;
        } catch (BusinessRuleException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/events/" + eventId;
        }
    }

    @PostMapping("/my-tickets/{ticketId}/cancel")
    public String cancel(@AuthenticationPrincipal UserDetails principal,
                         @PathVariable Long ticketId,
                         RedirectAttributes ra) {
        Attendee attendee = currentAttendee(principal);
        try {
            ticketService.cancel(attendee, ticketId);
            ra.addFlashAttribute("successMessage", "Ticket cancelled.");
        } catch (BusinessRuleException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/my-tickets";
    }

    @GetMapping("/my-tickets")
    public String myTickets(@AuthenticationPrincipal UserDetails principal, Model model) {
        Attendee attendee = currentAttendee(principal);
        model.addAttribute("tickets", ticketService.findAllForAttendee(attendee));
        return "tickets/my-tickets";
    }
}