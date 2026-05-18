package com.erbol.ems.ticket;

import com.erbol.ems.common.exception.BusinessRuleException;
import com.erbol.ems.common.exception.EventFullException;
import com.erbol.ems.common.exception.ResourceNotFoundException;
import com.erbol.ems.event.Event;
import com.erbol.ems.event.EventRepository;
import com.erbol.ems.event.EventStatus;
import com.erbol.ems.user.Attendee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             EventRepository eventRepository) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Register an attendee for an event.
     *
     * <p>This method is annotated @Retryable so Spring will automatically
     * re-execute it up to 3 times if an OptimisticLockingFailure is thrown —
     * which happens when two attendees race for the last seat. On retry,
     * a fresh Event entity (with the new version) is loaded and the capacity
     * check runs again.
     */
    @Override
    @Transactional
    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 50)
    )
    public Ticket register(Attendee attendee, Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException(
                "Cannot register for an event that is not PUBLISHED");
        }
        if (event.getStartAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException(
                "Cannot register for an event that has already started");
        }

        ticketRepository
            .findByAttendeeAndEventAndStatus(attendee, event, TicketStatus.ACTIVE)
            .ifPresent(t -> {
                throw new BusinessRuleException(
                    "You already have an active ticket for this event");
            });

        long activeTickets = ticketRepository.countByEventAndStatus(
            event, TicketStatus.ACTIVE);
        if (activeTickets >= event.getCapacity()) {
            throw new EventFullException(eventId);
        }

        // Trigger an optimistic-lock check on Event: even though we don't
        // change Event's state, we want concurrent registrations to be
        // serialized. We touch the version by saving the event explicitly.
        eventRepository.save(event);

        Ticket ticket = new Ticket(attendee, event);
        Ticket saved = ticketRepository.save(ticket);
        log.info("Registered ticket {} for attendee={} event={}",
            saved.getCode(), attendee.getId(), eventId);
        return saved;
    }

    @Override
    @Transactional
    public Ticket cancel(Attendee attendee, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        if (!ticket.isOwnedBy(attendee)) {
            throw new BusinessRuleException("This ticket does not belong to you");
        }

        Event event = ticket.getEvent();
        if (event.getStartAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException(
                "Cannot cancel a ticket for an event that has already started");
        }

        ticket.cancel();
        log.info("Cancelled ticket {} (attendee={})", ticket.getCode(), attendee.getId());
        return ticket;
    }

    @Override
    public List<Ticket> findAllForAttendee(Attendee attendee) {
        return ticketRepository.findAllByAttendeeWithEvent(attendee);
    }

    @Override
    public List<Ticket> findActiveForEvent(Long eventId) {
        return ticketRepository.findAllByEventIdAndStatus(eventId, TicketStatus.ACTIVE);
    }

    @Override
    public long countSeatsRemaining(Event event) {
        long active = ticketRepository.countByEventAndStatus(event, TicketStatus.ACTIVE);
        return Math.max(0, event.getCapacity() - active);
    }
}
