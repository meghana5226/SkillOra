package com.skillora.service;

import com.skillora.dto.session.SessionCreateRequest;
import com.skillora.dto.session.SessionResponse;
import com.skillora.entity.*;
import com.skillora.exception.BadRequestException;
import com.skillora.exception.ResourceNotFoundException;
import com.skillora.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;
    private final GamificationService gamificationService;

    /** requester = the learner requesting the session; receiver = the mentor/teacher. */
    @Transactional
    public SessionResponse create(Long requesterId, SessionCreateRequest request) {
        if (requesterId.equals(request.getReceiverId())) {
            throw new BadRequestException("You cannot schedule a session with yourself");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException("endTime must be after startTime");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        Session session = Session.builder()
                .requester(requester)
                .receiver(receiver)
                .skill(skill)
                .scheduledDate(request.getScheduledDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .meetingLink(request.getMeetingLink())
                .notes(request.getNotes())
                .status(SessionStatus.REQUESTED)
                .build();
        session = sessionRepository.save(session);

        notificationService.notify(receiver, "SESSION_REQUESTED",
                "New session request from " + requester.getName(),
                requester.getName() + " requested a " + skill.getName() + " session on " + session.getScheduledDate() + ".");

        return toResponse(session, requesterId);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> upcoming(Long userId) {
        return sessionRepository.findUpcomingForUser(userId, List.of(SessionStatus.REQUESTED, SessionStatus.CONFIRMED))
                .stream().map(s -> toResponse(s, userId)).toList();
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> history(Long userId) {
        return sessionRepository.findHistoryForUser(userId, List.of(SessionStatus.COMPLETED, SessionStatus.CANCELLED))
                .stream().map(s -> toResponse(s, userId)).toList();
    }

    @Transactional
    public SessionResponse confirm(Long receiverId, Long sessionId) {
        Session session = getOwned(sessionId, receiverId);
        if (!session.getReceiver().getId().equals(receiverId)) {
            throw new BadRequestException("Only the session's teacher can confirm it");
        }
        if (session.getStatus() != SessionStatus.REQUESTED) {
            throw new BadRequestException("Only requested sessions can be confirmed");
        }
        session.setStatus(SessionStatus.CONFIRMED);
        sessionRepository.save(session);

        notificationService.notify(session.getRequester(), "SESSION_CONFIRMED",
                session.getReceiver().getName() + " confirmed your session",
                "Your " + session.getSkill().getName() + " session on " + session.getScheduledDate() + " is confirmed.");

        return toResponse(session, receiverId);
    }

    @Transactional
    public SessionResponse cancel(Long userId, Long sessionId) {
        Session session = getOwned(sessionId, userId);
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new BadRequestException("A completed session cannot be cancelled");
        }
        session.setStatus(SessionStatus.CANCELLED);
        sessionRepository.save(session);

        User other = session.getRequester().getId().equals(userId) ? session.getReceiver() : session.getRequester();
        notificationService.notify(other, "SESSION_CANCELLED",
                "A session was cancelled",
                "Your " + session.getSkill().getName() + " session on " + session.getScheduledDate() + " was cancelled.");

        return toResponse(session, userId);
    }

    @Transactional
    public SessionResponse complete(Long userId, Long sessionId) {
        Session session = getOwned(sessionId, userId);
        if (session.getStatus() != SessionStatus.CONFIRMED) {
            throw new BadRequestException("Only confirmed sessions can be marked complete");
        }
        session.setStatus(SessionStatus.COMPLETED);
        sessionRepository.save(session);

        gamificationService.awardPoints(session.getRequester(), GamificationService.XP_SESSION_COMPLETE,
                "Completed a " + session.getSkill().getName() + " learning session");
        gamificationService.awardPoints(session.getReceiver(), GamificationService.XP_SESSION_COMPLETE,
                "Taught a " + session.getSkill().getName() + " session");

        return toResponse(session, userId);
    }

    private Session getOwned(Long sessionId, Long userId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        boolean participant = session.getRequester().getId().equals(userId) || session.getReceiver().getId().equals(userId);
        if (!participant) {
            throw new BadRequestException("You are not part of this session");
        }
        return session;
    }

    private SessionResponse toResponse(Session s, Long viewerId) {
        boolean hasReview = !reviewRepository.findBySessionId(s.getId()).isEmpty();
        return SessionResponse.builder()
                .id(s.getId())
                .requesterId(s.getRequester().getId())
                .requesterName(s.getRequester().getName())
                .receiverId(s.getReceiver().getId())
                .receiverName(s.getReceiver().getName())
                .skillId(s.getSkill().getId())
                .skillName(s.getSkill().getName())
                .scheduledDate(s.getScheduledDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .meetingLink(s.getMeetingLink())
                .status(s.getStatus().name())
                .notes(s.getNotes())
                .createdAt(s.getCreatedAt())
                .hasReview(hasReview)
                .build();
    }
}
