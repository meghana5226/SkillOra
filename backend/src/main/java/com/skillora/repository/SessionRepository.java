package com.skillora.repository;

import com.skillora.entity.Session;
import com.skillora.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("SELECT s FROM Session s WHERE (s.requester.id = :userId OR s.receiver.id = :userId) " +
           "AND s.status IN :statuses ORDER BY s.scheduledDate ASC, s.startTime ASC")
    List<Session> findUpcomingForUser(@Param("userId") Long userId, @Param("statuses") List<SessionStatus> statuses);

    @Query("SELECT s FROM Session s WHERE (s.requester.id = :userId OR s.receiver.id = :userId) " +
           "AND s.status IN :statuses ORDER BY s.scheduledDate DESC, s.startTime DESC")
    List<Session> findHistoryForUser(@Param("userId") Long userId, @Param("statuses") List<SessionStatus> statuses);

    long countByStatus(SessionStatus status);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.receiver.id = :userId AND s.status = 'COMPLETED'")
    long countCompletedAsTeacher(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.requester.id = :userId AND s.status = 'COMPLETED'")
    long countCompletedAsLearner(@Param("userId") Long userId);
}
