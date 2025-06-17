package finalmission.running.service;

import finalmission.member.domain.Member;
import finalmission.member.domain.Role;
import finalmission.member.dto.response.LoginInfo;
import finalmission.member.exception.UnauthorizedException;
import finalmission.member.repository.MemberRepository;
import finalmission.running.domain.Participant;
import finalmission.running.domain.RunningSession;
import finalmission.running.dto.request.ReservationRequest;
import finalmission.running.dto.request.UpdateRequest;
import finalmission.running.dto.response.ReservationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RunningServiceTest {

    @Autowired
    RunningService runningService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RunningReservationService runningReservationService;

    @Autowired
    ParticipantService participantService;

    private ReservationRequest request;
    private LoginInfo loginInfo;

    @BeforeEach
    void setUp() {
        request = new ReservationRequest(
            "11B00000",
            List.of(),
            LocalDate.now().plusDays(1),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0)
        );
        Member member = memberRepository.save(Member.createWithoutId("드라고", "email@email.com", "1234", Role.USER));
        loginInfo = new LoginInfo(member.getId(), member.getRole());
    }

    @Test
    void 세션_생성자와_참가자는_세션_정보를_조회할_수_있다() {
        // given
        runningReservationService.createRunningReservation(request, loginInfo);

        // when
        ReservationResponse reservationResponse = runningService.searchInfos(1L, loginInfo);

        // then
        assertThat(reservationResponse.id()).isEqualTo(1L);
    }

    @Test
    void 세션_생성자와_참가자가_아니라면_세션_정보를_조회할_수_없다() {
        // given
        runningReservationService.createRunningReservation(request, loginInfo);
        memberRepository.save(Member.createWithoutId("2번멤버", "2", "3", Role.USER));

        // when & then
        LoginInfo wrongInfo = new LoginInfo(2L, Role.USER);
        assertThatThrownBy(() -> runningService.searchInfos(1L, wrongInfo))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("세션 생성자와 참가자만 세션 정보를 열람할 수 있습니다.");
    }

    @Test
    void 세션_시간을_수정할_수_있다() {
        // given
        ReservationResponse response = runningReservationService.createRunningReservation(request, loginInfo);
        Long sessionId = response.id();

        // when
        LocalTime newStartAt = LocalTime.of(12, 0);
        LocalTime newEndTime = LocalTime.of(13, 0);
        UpdateRequest updateRequest = new UpdateRequest(newStartAt, newEndTime);
        ReservationResponse updated = runningService.updateRunningTime(sessionId, updateRequest, loginInfo);

        // then
        assertThat(updated.startAt()).isEqualTo(newStartAt);
        assertThat(updated.endTime()).isEqualTo(newEndTime);
    }


    @Test
    void 세션_생성자가_아닌_사용자가_세션_시간을_수정하면_예외가_발생한다() {
        // given
        ReservationResponse response = runningReservationService.createRunningReservation(request, loginInfo);
        Long sessionId = response.id();

        Member other = memberRepository.save(Member.createWithoutId("다른사람", "other@email.com", "pass", Role.USER));
        LoginInfo otherLogin = new LoginInfo(other.getId(), other.getRole());

        LocalTime newStartAt = LocalTime.of(14, 0);
        LocalTime newEndTime = LocalTime.of(15, 0);
        UpdateRequest updateRequest = new UpdateRequest(newStartAt, newEndTime);

        // when & then
        assertThatThrownBy(() -> runningService.updateRunningTime(sessionId, updateRequest, otherLogin))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("세션 수정/삭제는 생성자만 가능합니다.");
    }

    @Test
    void 세션을_정상적으로_삭제할_수_있다() {
        // given
        ReservationResponse response = runningReservationService.createRunningReservation(request, loginInfo);
        Long sessionId = response.id();

        // when
        runningService.deleteSession(sessionId, loginInfo);

        // then
        assertThatThrownBy(() -> runningService.searchInfos(sessionId, loginInfo))
            .isInstanceOf(finalmission.running.exception.ReservationException.class)
            .hasMessage("러닝 세션을 찾을 수 없습니다.");
    }

    @Test
    void 세션_생성자가_아닌_사용자가_삭제를_시도하면_예외가_발생한다() {
        // given
        ReservationResponse response = runningReservationService.createRunningReservation(request, loginInfo);
        Long sessionId = response.id();

        Member other = memberRepository.save(Member.createWithoutId("삭제안됨", "other@no.com", "1234", Role.USER));
        LoginInfo otherLogin = new LoginInfo(other.getId(), other.getRole());

        // when & then
        assertThatThrownBy(() -> runningService.deleteSession(sessionId, otherLogin))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("세션 수정/삭제는 생성자만 가능합니다.");
    }

    @Test
    void 참가자는_세션_참가를_취소할_수_있다() {
        // given
        ReservationResponse response = runningReservationService.createRunningReservation(request, loginInfo);
        RunningSession runningSession = runningReservationService.findRunningSession(response.id());

        Member newMember = memberRepository.save(Member.createWithoutId("참가자", "join@user.com", "pass", Role.USER));
        participantService.createParticipant(Participant.createWithoutId(runningSession, newMember));

        // when
        LoginInfo participantInfo = new LoginInfo(newMember.getId(), newMember.getRole());
        runningService.cancelSessionJoin(runningSession.getId(), participantInfo);

        // then
        assertThatThrownBy(() -> runningService.searchInfos(response.id(), participantInfo))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("세션 생성자와 참가자만 세션 정보를 열람할 수 있습니다.");
    }

    @Test
    void 참가자가_아닌_사용자가_참가_취소를_시도하면_예외가_발생한다() {
        // given
        ReservationResponse response = runningReservationService.createRunningReservation(request, loginInfo);
        Long sessionId = response.id();

        Member nonParticipant = memberRepository.save(Member.createWithoutId("비참가자", "nopart@user.com", "pass", Role.USER));
        LoginInfo nonParticipantInfo = new LoginInfo(nonParticipant.getId(), nonParticipant.getRole());

        // when & then
        assertThatThrownBy(() -> runningService.cancelSessionJoin(sessionId, nonParticipantInfo))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("세션 참가 취소는 참가자만 가능합니다.");
    }
}