package com.erbol.ems.event;

import com.erbol.ems.category.Category;
import com.erbol.ems.common.BaseEntity;
import com.erbol.ems.common.exception.BusinessRuleException;
import com.erbol.ems.user.Organizer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "location", nullable = false, length = 200)
    private String location;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected Event() {
        // required by JPA
    }

    public Event(String title, String description, String location,
                 LocalDateTime startAt, LocalDateTime endAt,
                 int capacity, BigDecimal price,
                 Organizer organizer, Category category) {
        validateTimeRange(startAt, endAt);
        validateCapacity(capacity);
        validatePrice(price);

        this.title = title;
        this.description = description;
        this.location = location;
        this.startAt = startAt;
        this.endAt = endAt;
        this.capacity = capacity;
        this.price = price;
        this.organizer = organizer;
        this.category = category;
        this.status = EventStatus.DRAFT;
    }

    // ===== Business operations (rich domain model) =====

    /**
     * Transition this event to PUBLISHED.
     * Only allowed from DRAFT, and the event must not have already started.
     */
    public void publish() {
        if (status != EventStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Only DRAFT events can be published (current: " + status + ")");
        }
        if (startAt.isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "Cannot publish an event whose start time is in the past");
        }
        this.status = EventStatus.PUBLISHED;
    }

    /**
     * Transition this event to CANCELLED. Allowed from DRAFT or PUBLISHED.
     * Once cancelled, an event is final and cannot be modified or republished.
     */
    public void cancel() {
        if (status == EventStatus.CANCELLED) {
            throw new BusinessRuleException("Event is already cancelled");
        }
        this.status = EventStatus.CANCELLED;
    }

    /**
     * Update mutable details. Allowed only before the event has started
     * and only while the event is not cancelled.
     */
    public void updateDetails(String title, String description, String location,
                              LocalDateTime startAt, LocalDateTime endAt,
                              int capacity, BigDecimal price, Category category) {
        if (status.isFinal()) {
            throw new BusinessRuleException("Cannot edit a cancelled event");
        }
        if (this.startAt.isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "Cannot edit an event whose start time is in the past");
        }
        validateTimeRange(startAt, endAt);
        validateCapacity(capacity);
        validatePrice(price);

        this.title = title;
        this.description = description;
        this.location = location;
        this.startAt = startAt;
        this.endAt = endAt;
        this.capacity = capacity;
        this.price = price;
        this.category = category;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    /** True if this event belongs to the given organizer. */
    public boolean isOwnedBy(Organizer maybeOwner) {
        return this.organizer != null
                && maybeOwner != null
                && this.organizer.getId() != null
                && this.organizer.getId().equals(maybeOwner.getId());
    }

    public boolean isFree() {
        return price.compareTo(BigDecimal.ZERO) == 0;
    }

    // ===== Validation helpers =====

    private static void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BusinessRuleException("Start and end time are required");
        }
        if (!start.isBefore(end)) {
            throw new BusinessRuleException("Start time must be before end time");
        }
    }

    private static void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new BusinessRuleException("Capacity must be a positive number");
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Price must be zero or positive");
        }
    }

    // ===== Getters (no public setters for invariants) =====

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public int getCapacity() { return capacity; }
    public BigDecimal getPrice() { return price; }
    public EventStatus getStatus() { return status; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public Organizer getOrganizer() { return organizer; }
    public Category getCategory() { return category; }
}