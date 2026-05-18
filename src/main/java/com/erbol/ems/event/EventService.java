package com.erbol.ems.event;

import com.erbol.ems.event.dto.EventFormDto;
import com.erbol.ems.user.Organizer;
import com.erbol.ems.event.dto.EventCardDto;
import com.erbol.ems.event.dto.EventDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface EventService {

    /** Create a new event in DRAFT status for the given organizer. */
    Event create(Organizer organizer, EventFormDto dto);

    /**
     * Update the given event's details. The current user must own the event.
     *
     * @throws com.erbol.ems.common.exception.BusinessRuleException
     *         if the event is cancelled, started, or not owned by this user.
     */
    Event update(Long eventId, Organizer currentUser, EventFormDto dto);

    /** Publish the event. Must be owned by the user and currently DRAFT. */
    Event publish(Long eventId, Organizer currentUser);

    /** Cancel the event. Must be owned by the user. */
    Event cancel(Long eventId, Organizer currentUser);

    /** Fetch the event, asserting that it is owned by the current user. */
    Event findByIdForOwner(Long eventId, Organizer currentUser);

    /** All events created by the given organizer, newest first. */
    List<Event> findAllByOrganizer(Organizer organizer);

    /** Public catalog: only PUBLISHED events, paginated. */
    Page<EventCardDto> findPublicCatalog(Pageable pageable);

    /** Public event detail. Only PUBLISHED events are visible. */
    EventDetailDto findPublicById(Long eventId);
}
