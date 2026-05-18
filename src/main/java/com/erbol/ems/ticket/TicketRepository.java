package com.erbol.ems.ticket;

import com.erbol.ems.event.Event;
import com.erbol.ems.user.Attendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCode(String code);

    /**
     * Find an ACTIVE ticket for this attendee+event combination.
     * Used to prevent duplicate registrations.
     */
    Optional<Ticket> findByAttendeeAndEventAndStatus(
            Attendee attendee, Event event, TicketStatus status);

    /**
     * Count of ACTIVE tickets for an event — used to check capacity.
     */
    long countByEventAndStatus(Event event, TicketStatus status);

    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.organizer " +
            "WHERE t.attendee = :attendee " +
            "ORDER BY e.startAt DESC")
    List<Ticket> findAllByAttendeeWithEvent(@Param("attendee") Attendee attendee);

    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.attendee " +
            "WHERE t.event.id = :eventId AND t.status = :status " +
            "ORDER BY t.createdAt ASC")
    List<Ticket> findAllByEventIdAndStatus(
            @Param("eventId") Long eventId, @Param("status") TicketStatus status);
}