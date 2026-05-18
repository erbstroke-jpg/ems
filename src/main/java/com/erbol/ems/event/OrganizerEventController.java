package com.erbol.ems.event;

import com.erbol.ems.category.Category;
import com.erbol.ems.category.CategoryService;
import com.erbol.ems.common.exception.BusinessRuleException;
import com.erbol.ems.event.dto.EventFormDto;
import com.erbol.ems.user.Organizer;
import com.erbol.ems.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.erbol.ems.ticket.TicketService;

import java.util.List;

@Controller
@RequestMapping("/organizer/events")
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerEventController {

    private final EventService eventService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    private final TicketService ticketService;

    public OrganizerEventController(EventService eventService,
                                    CategoryService categoryService,
                                    UserRepository userRepository,
                                    TicketService ticketService) {
        this.eventService = eventService;
        this.categoryService = categoryService;
        this.userRepository = userRepository;
        this.ticketService = ticketService;
    }

    private Organizer currentOrganizer(UserDetails principal) {
        return userRepository.findOrganizerByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user is not an organizer: " + principal.getUsername()));
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        Organizer organizer = currentOrganizer(principal);
        model.addAttribute("events", eventService.findAllByOrganizer(organizer));
        return "organizer/events/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("eventFormDto", new EventFormDto());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("formMode", "create");
        return "organizer/events/form";
    }

    @PostMapping("/new")
    public String create(@AuthenticationPrincipal UserDetails principal,
                         @Valid @ModelAttribute EventFormDto dto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formMode", "create");
            return "organizer/events/form";
        }
        Organizer organizer = currentOrganizer(principal);
        try {
            Event created = eventService.create(organizer, dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Event '" + created.getTitle() + "' created as DRAFT.");
        } catch (BusinessRuleException ex) {
            bindingResult.reject("event.create.failed", ex.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formMode", "create");
            return "organizer/events/form";
        }
        return "redirect:/organizer/events";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@AuthenticationPrincipal UserDetails principal,
                           @PathVariable Long id,
                           Model model) {
        Organizer organizer = currentOrganizer(principal);
        Event event = eventService.findByIdForOwner(id, organizer);

        EventFormDto dto = EventFormDto.builder()
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .capacity(event.getCapacity())
                .price(event.getPrice())
                .categoryId(event.getCategory().getId())
                .build();

        model.addAttribute("eventFormDto", dto);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("eventId", id);
        model.addAttribute("formMode", "edit");
        return "organizer/events/form";
    }
    @GetMapping("/{id}/attendees")
    public String attendees(@AuthenticationPrincipal UserDetails principal,
                            @PathVariable Long id,
                            Model model) {
        Organizer organizer = currentOrganizer(principal);
        Event event = eventService.findByIdForOwner(id, organizer);

        model.addAttribute("event", event);
        model.addAttribute("tickets", ticketService.findActiveForEvent(id));
        model.addAttribute("seatsRemaining", ticketService.countSeatsRemaining(event));
        return "organizer/events/attendees";
    }

    @PostMapping("/{id}/edit")
    public String update(@AuthenticationPrincipal UserDetails principal,
                         @PathVariable Long id,
                         @Valid @ModelAttribute EventFormDto dto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("eventId", id);
            model.addAttribute("formMode", "edit");
            return "organizer/events/form";
        }
        Organizer organizer = currentOrganizer(principal);
        try {
            eventService.update(id, organizer, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Event updated.");
        } catch (BusinessRuleException ex) {
            bindingResult.reject("event.update.failed", ex.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("eventId", id);
            model.addAttribute("formMode", "edit");
            return "organizer/events/form";
        }
        return "redirect:/organizer/events";
    }

    @PostMapping("/{id}/publish")
    public String publish(@AuthenticationPrincipal UserDetails principal,
                          @PathVariable Long id,
                          RedirectAttributes redirectAttributes) {
        Organizer organizer = currentOrganizer(principal);
        try {
            eventService.publish(id, organizer);
            redirectAttributes.addFlashAttribute("successMessage", "Event published.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/organizer/events";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@AuthenticationPrincipal UserDetails principal,
                         @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        Organizer organizer = currentOrganizer(principal);
        try {
            eventService.cancel(id, organizer);
            redirectAttributes.addFlashAttribute("successMessage", "Event cancelled.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/organizer/events";
    }
}