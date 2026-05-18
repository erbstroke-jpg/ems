package com.erbol.ems.event;

import com.erbol.ems.category.Category;
import com.erbol.ems.category.CategoryService;
import com.erbol.ems.common.exception.BusinessRuleException;
import com.erbol.ems.common.exception.ResourceNotFoundException;
import com.erbol.ems.event.dto.EventFormDto;
import com.erbol.ems.user.Organizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.erbol.ems.event.dto.EventCardDto;
import com.erbol.ems.event.dto.EventDetailDto;
import com.erbol.ems.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.erbol.ems.ticket.TicketRepository;
import com.erbol.ems.ticket.TicketStatus;
import com.erbol.ems.common.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final EventMapper eventMapper;
    private final TicketRepository ticketRepository;
    private final FileStorageService fileStorageService;

    public EventServiceImpl(EventRepository eventRepository,
                            CategoryService categoryService,
                            EventMapper eventMapper, TicketRepository ticketRepository, FileStorageService fileStorageService) {
        this.eventRepository = eventRepository;
        this.categoryService = categoryService;
        this.eventMapper = eventMapper;
        this.ticketRepository = ticketRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public Event create(Organizer organizer, EventFormDto dto) {
        Category category = categoryService.findById(dto.getCategoryId());

        Event event = new Event(
                dto.getTitle().trim(),
                dto.getDescription().trim(),
                dto.getLocation().trim(),
                dto.getStartAt(),
                dto.getEndAt(),
                dto.getCapacity(),
                dto.getPrice(),
                organizer,
                category
        );

        // Cover image upload
        MultipartFile cover = dto.getCoverImage();
        if (cover != null && !cover.isEmpty()) {
            String url = fileStorageService.storeEventCover(cover);
            event.setCoverImageUrl(url);
        }

        Event saved = eventRepository.save(event);
        log.info("Created event id={} for organizer id={}",
                saved.getId(), organizer.getId());
        return saved;
    }

    @Override
    public Page<EventCardDto> findPublicCatalog(Pageable pageable) {
        Page<Event> events = eventRepository
                .findAllByStatusOrderByStartAtAsc(EventStatus.PUBLISHED, pageable);
        return events.map(eventMapper::toCardDto);
    }
    @Override
    public EventDetailDto findPublicById(Long eventId) {
        Event event = eventRepository.findByIdWithDetails(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Event", eventId);
        }

        long activeTickets = ticketRepository.countByEventAndStatus(event, TicketStatus.ACTIVE);
        long seatsRemaining = Math.max(0, event.getCapacity() - activeTickets);

        EventDetailDto dto = eventMapper.toDetailDto(event);
        dto.setSeatsRemaining(seatsRemaining);
        dto.setSoldOut(seatsRemaining == 0);
        return dto;
    }

    @Override
    @Transactional
    public Event update(Long eventId, Organizer currentUser, EventFormDto dto) {
        Event event = findByIdForOwner(eventId, currentUser);
        Category category = categoryService.findById(dto.getCategoryId());

        event.updateDetails(
                dto.getTitle().trim(),
                dto.getDescription().trim(),
                dto.getLocation().trim(),
                dto.getStartAt(),
                dto.getEndAt(),
                dto.getCapacity(),
                dto.getPrice(),
                category
        );

        // Cover image replacement
        MultipartFile cover = dto.getCoverImage();
        if (cover != null && !cover.isEmpty()) {
            String oldUrl = event.getCoverImageUrl();
            String newUrl = fileStorageService.storeEventCover(cover);
            event.setCoverImageUrl(newUrl);
            // best effort: delete previous file
            if (oldUrl != null) {
                fileStorageService.deleteIfExists(oldUrl);
            }
        }

        log.info("Updated event id={}", eventId);
        return event;
    }

    @Override
    @Transactional
    public Event publish(Long eventId, Organizer currentUser) {
        Event event = findByIdForOwner(eventId, currentUser);
        event.publish();
        log.info("Published event id={}", eventId);
        return event;
    }

    @Override
    @Transactional
    public Event cancel(Long eventId, Organizer currentUser) {
        Event event = findByIdForOwner(eventId, currentUser);
        event.cancel();
        log.info("Cancelled event id={}", eventId);
        return event;
    }

    @Override
    public Event findByIdForOwner(Long eventId, Organizer currentUser) {
        Event event = eventRepository.findByIdWithDetails(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));
        if (!event.isOwnedBy(currentUser)) {
            throw new BusinessRuleException(
                "You do not have access to this event");
        }
        return event;
    }

    @Override
    public List<Event> findAllByOrganizer(Organizer organizer) {
        return eventRepository.findAllByOrganizerOrderByStartAtDesc(organizer);
    }
}
