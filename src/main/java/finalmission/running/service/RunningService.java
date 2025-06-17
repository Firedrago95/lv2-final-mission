package finalmission.running.service;

import finalmission.member.domain.Member;
import finalmission.member.dto.response.LoginInfo;
import finalmission.member.exception.UnauthorizedException;
import finalmission.member.service.LoginService;
import finalmission.running.domain.Participant;
import finalmission.running.domain.RunningSession;
import finalmission.running.dto.request.ReservationRequest;
import finalmission.running.dto.request.UpdateRequest;
import finalmission.running.dto.response.ReservationResponse;
import finalmission.running.dto.response.SessionSimpleResponse;
import finalmission.running.exception.ReservationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RunningService {

    private final RunningReservationService runningReservationService;
    private final WeatherService weatherService;
    private final LoginService loginService;
    private final ParticipantService participantService;

    public ReservationResponse createRunningReservation(ReservationRequest request, LoginInfo loginInfo) {
        weatherService.checkRunnableWeather(request);
        return runningReservationService.createRunningReservation(request, loginInfo);
    }

    public ReservationResponse joinRunningReservation(Long id, LoginInfo loginInfo) {
        RunningSession runningSession = runningReservationService.findRunningSession(id);
        Member member = loginService.findMember(loginInfo.id());

        Participant participant = participantService.createParticipant(Participant.createWithoutId(runningSession, member));

        RunningSession resultSession = participant.getRunningSession();
        return ReservationResponse.from(resultSession);
    }

    public List<SessionSimpleResponse> searchAllSimpleInfos() {
        return runningReservationService.searchAllSimpleInfos();
    }

    public ReservationResponse searchInfos(Long id, LoginInfo loginInfo) {
        RunningSession runningSession = runningReservationService.findRunningSession(id);
        Member member = loginService.findMember(loginInfo.id());
        validateCreatorOrParticipants(runningSession, member);
        return ReservationResponse.from(runningSession);
    }

    private void validateCreatorOrParticipants(RunningSession runningSession, Member findMember) {
        if (!runningSession.isCreatorOrParticipants(findMember)) {
            throw new UnauthorizedException("세션 생성자와 참가자만 세션 정보를 열람할 수 있습니다.");
        }
    }

    @Transactional
    public ReservationResponse updateRunningTime(Long id, UpdateRequest updateRequest, LoginInfo loginInfo) {
        RunningSession runningSession = runningReservationService.findRunningSession(id);
        Member member = loginService.findMember(loginInfo.id());

        validateCreator(runningSession, member);
        validateTime(updateRequest);
        runningSession.setStartAt(updateRequest.startAt());
        runningSession.setEndTime(updateRequest.endTime());
        return ReservationResponse.from(runningSession);
    }

    private void validateCreator(RunningSession runningSession, Member member) {
        if (!runningSession.isCreator(member)) {
            throw new UnauthorizedException("세션 수정은 생성자만 가능합니다.");
        }
    }

    private void validateTime(UpdateRequest updateRequest) {
        if (updateRequest.startAt().isAfter(updateRequest.endTime())) {
            throw new ReservationException("시작시간과 종료시간을 다시 확인해주세요.");
        }
    }
}
