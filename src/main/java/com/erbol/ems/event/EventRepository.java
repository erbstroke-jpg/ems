package com.erbol.ems.event;

import com.erbol.ems.user.Organizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e JOIN FETCH e.category JOIN FETCH e.organizer " +
            "WHERE e.organizer = :organizer ORDER BY e.startAt DESC")
    List<Event> findAllByOrganizerOrderByStartAtDesc(@Param("organizer") Organizer organizer);

    @Query("SELECT e FROM Event e JOIN FETCH e.category JOIN FETCH e.organizer " +
            "WHERE e.status = :status ORDER BY e.startAt ASC")
    Page<Event> findAllByStatusOrderByStartAtAsc(@Param("status") EventStatus status, Pageable pageable);

    @Query("SELECT e FROM Event e JOIN FETCH e.category JOIN FETCH e.organizer WHERE e.id = :id")
    Optional<Event> findByIdWithDetails(@Param("id") Long id);

    long countByOrganizer(Organizer organizer);
}