package com.erbol.ems.ticket;

import com.erbol.ems.user.Attendee;

import java.util.List;

public interface TicketService {

    /**
     * Register the given attendee for the given event.
     * The full operation runs in a single transaction with optimistic locking
     * on the Event entity to prevent overbooking under concurrent requests.
     *
     * @throws com.erbol.ems.common.exception.ResourceNotFoundException
     *         if the event does not exist
     * @throws com.erbol.ems.common.exception.BusinessRuleException
     *         if the event is not PUBLISHED, already started, or the attendee
     *         already holds an ACTIVE ticket for this event
     * @throws com.erbol.ems.common.exception.EventFullException
     *         if capacity is already reached
     */
    Ticket register(Attendee attendee, Long eventId);

    /**
     * Cancel an ACTIVE ticket. Must be owned by the given attendee.
     */
    Ticket cancel(Attendee attendee, Long ticketId);

    /** All tickets owned by an attendee, newest events first. */
    List<Ticket> findAllForAttendee(Attendee attendee);

    /** All ACTIVE tickets for an event — for organizer's attendee list. */
    List<Ticket> findActiveForEvent(Long eventId);

    /** Available seat count for an event. */
    long countSeatsRemaining(com.erbol.ems.event.Event event);
}