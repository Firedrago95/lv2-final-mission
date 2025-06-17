package finalmission.running.service;

import finalmission.member.domain.Member;
import finalmission.member.dto.response.LoginInfo;
import finalmission.member.service.LoginService;
import finalmission.running.domain.RunningSession;
import finalmission.running.dto.request.ReservationRequest;
import finalmission.running.dto.response.ReservationResponse;
import finalmission.running.dto.response.SessionSimpleResponse;
import finalmission.running.exception.ReservationException;
import finalmission.running.repository.RunningReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RunningReservationService {

    private final RunningReservationRepository reservationRepository;
    private final LoginService loginService;

    @Transactional
    public ReservationResponse createRunningReservation(ReservationRequest request, LoginInfo loginInfo) {
        Member creator = loginService.findMember(loginInfo.id());

        RunningSession runningSession = RunningSession.createWithoutId(
            creator,
            request.date(),
            request.startAt(),
            request.endTime()
        );

        RunningSession save = reservationRepository.save(runningSession);

        return ReservationResponse.from(save);
    }

    public RunningSession findRunningSession(Long id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new ReservationException("러닝 세션을 찾을 수 없습니다."));
    }

    public List<SessionSimpleResponse> searchAllSimpleInfos() {
        List<RunningSession> allSessions = reservationRepository.findAll();

        return allSessions.stream()
            .map(session -> new SessionSimpleResponse(
                session.getDate(),
                session.getStartAt(),
                session.getEndTime())
            ).toList();
    }

    @Transactional
    public void delete(RunningSession runningSession) {
        reservationRepository.delete(runningSession);
    }
}
