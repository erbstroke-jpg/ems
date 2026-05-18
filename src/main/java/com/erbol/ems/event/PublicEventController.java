package com.erbol.ems.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.erbol.ems.event.dto.EventCardDto;

@Controller
public class PublicEventController {

    private static final int PAGE_SIZE = 9;

    private final EventService eventService;

    public PublicEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/events")
    public String catalog(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(Math.max(0, page), PAGE_SIZE);
        Page<EventCardDto> events = eventService.findPublicCatalog(pageable);

        model.addAttribute("events", events);
        model.addAttribute("currentPage", events.getNumber());
        model.addAttribute("totalPages", events.getTotalPages());
        return "events/catalog";
    }

    @GetMapping("/events/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findPublicById(id));
        return "events/details";
    }
}