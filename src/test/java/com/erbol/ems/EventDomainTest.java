package com.erbol.ems;

import com.erbol.ems.category.Category;
import com.erbol.ems.common.exception.BusinessRuleException;
import com.erbol.ems.event.Event;
import com.erbol.ems.event.EventStatus;
import com.erbol.ems.user.Organizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Event domain rules")
class EventDomainTest {

    private Event sampleEvent() {
        return new Event(
                "Java Meetup", "Description here", "Bishkek",
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(3).plusHours(2),
                50, BigDecimal.ZERO,
                new Organizer("Org", "org@test.com", "hash"),
                new Category("Tech", null)
        );
    }

    @Test
    @DisplayName("new event is DRAFT")
    void newEventIsDraft() {
        Event event = sampleEvent();
        assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    @DisplayName("can publish a DRAFT event")
    void publishDraft() {
        Event event = sampleEvent();
        event.publish();
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    @DisplayName("cannot publish a cancelled event")
    void cannotPublishCancelled() {
        Event event = sampleEvent();
        event.cancel();

        assertThatThrownBy(event::publish)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Only DRAFT");
    }

    @Test
    @DisplayName("rejects end time before start time")
    void rejectsInvalidTimeRange() {
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = start.minusHours(1);

        assertThatThrownBy(() -> new Event(
                "X", "desc", "loc", start, end, 10, BigDecimal.ZERO,
                new Organizer("o", "o@t.com", "h"),
                new Category("c", null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Start time must be before end time");
    }

    @Test
    @DisplayName("rejects non-positive capacity")
    void rejectsZeroCapacity() {
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        assertThatThrownBy(() -> new Event(
                "X", "desc", "loc", start, start.plusHours(1), 0, BigDecimal.ZERO,
                new Organizer("o", "o@t.com", "h"),
                new Category("c", null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Capacity");
    }
}