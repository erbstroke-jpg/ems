package com.erbol.ems.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.erbol.ems.user.Attendee;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("select o from Organizer o where o.email = :email")
    Optional<Organizer> findOrganizerByEmail(@Param("email") String email);

    @Query("select a from Attendee a where a.email = :email")
    Optional<Attendee> findAttendeeByEmail(@Param("email") String email);
}