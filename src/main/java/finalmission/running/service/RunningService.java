package finalmission.running.service;

import finalmission.member.domain.Member;
import finalmission.member.dto.response.LoginInfo;
import finalmission.member.service.LoginService;
import finalmission.running.domain.Participant;
import finalmission.running.domain.RunningSession;
import finalmission.running.dto.request.ReservationRequest;
import finalmission.running.dto.response.ReservationResponse;
import finalmission.running.dto.response.SessionSimpleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
