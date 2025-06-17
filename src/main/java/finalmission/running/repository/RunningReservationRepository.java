package finalmission.running.repository;

import finalmission.running.domain.RunningSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RunningReservationRepository extends JpaRepository<RunningSession, Long> {

    @Query("""
        SELECT DISTINCT rs
        FROM RunningSession rs
        JOIN FETCH rs.creator
        LEFT JOIN FETCH rs.participants
        """)
    Optional<RunningSession> findById(Long id);
}
