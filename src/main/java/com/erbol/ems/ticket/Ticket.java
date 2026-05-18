package com.erbol.ems.ticket;

import com.erbol.ems.common.BaseEntity;
import com.erbol.ems.common.exception.BusinessRuleException;
import com.erbol.ems.event.Event;
import com.erbol.ems.user.Attendee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(name = "tickets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ticket_code", columnNames = "code"
        ))
public class Ticket extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 36)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendee_id", nullable = false)
    private Attendee attendee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    protected Ticket() {
        // required by JPA
    }

    public Ticket(Attendee attendee, Event event) {
        if (attendee == null || event == null) {
            throw new IllegalArgumentException("Attendee and event are required");
        }
        this.attendee = attendee;
        this.event = event;
        this.code = UUID.randomUUID().toString();
        this.status = TicketStatus.ACTIVE;
    }

    public void cancel() {
        if (status != TicketStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Only ACTIVE tickets can be cancelled (current: " + status + ")");
        }
        this.status = TicketStatus.CANCELLED;
    }

    public void markUsed() {
        if (status != TicketStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Only ACTIVE tickets can be marked as used");
        }
        this.status = TicketStatus.USED;
    }

    public boolean isOwnedBy(Attendee maybeOwner) {
        return this.attendee != null
                && maybeOwner != null
                && this.attendee.getId() != null
                && this.attendee.getId().equals(maybeOwner.getId());
    }

    // ===== getters =====

    public String getCode() { return code; }
    public TicketStatus getStatus() { return status; }
    public Attendee getAttendee() { return attendee; }
    public Event getEvent() { return event; }
}