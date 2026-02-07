package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.ParticipationRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, ParticipationRequest.Status status);

    @Query("SELECT COUNT(r) FROM ParticipationRequest r " +
            "WHERE r.event.id = :eventId AND r.status = :status")
    Long countByEventIdAndStatus(@Param("eventId") Long eventId,
                                 @Param("status") ParticipationRequest.Status status);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId);

    List<ParticipationRequest> findByIdIn(List<Long> requestIds);
}