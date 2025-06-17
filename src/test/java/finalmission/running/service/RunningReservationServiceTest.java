package finalmission.running.service;

import finalmission.member.domain.Member;
import finalmission.member.domain.Role;
import finalmission.member.dto.response.LoginInfo;
import finalmission.member.exception.UnauthorizedException;
import finalmission.member.repository.MemberRepository;
import finalmission.running.dto.request.ReservationRequest;
import finalmission.running.dto.response.ReservationResponse;
import finalmission.running.dto.response.SessionSimpleResponse;
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
class RunningReservationServiceTest {

    @Autowired
    private RunningReservationService runningReservationService;

    @Autowired
    private MemberRepository memberRepository;

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
        memberRepository.save(Member.createWithoutId("드라고", "email@email.com", "1234", Role.USER));
        loginInfo = new LoginInfo(1L, Role.USER);
    }

    @Test
    void 예약을_저장할_수_있다() {
        // when
        ReservationResponse result = runningReservationService.createRunningReservation(request, loginInfo);

        // then
        ReservationResponse expected = new ReservationResponse(1L,
            "드라고",
            LocalDate.now().plusDays(1),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            List.of()
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void 존재하지_않는_사용자는_예약을_할_수_없다() {
        //given
        LoginInfo wrongUser = new LoginInfo(999L, Role.USER);

        // when & then
        assertThatThrownBy(() -> runningReservationService.createRunningReservation(request, wrongUser))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("사용자를 찾을 수 없습니다.");

    }

    @Test
    void 모든_세션을_조회할_수_있다() {
        // given
        runningReservationService.createRunningReservation(request, loginInfo);

        // when
        List<SessionSimpleResponse> result = runningReservationService.searchAllSimpleInfos();

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.getFirst().startAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.getFirst().endTime()).isEqualTo(LocalTime.of(11, 0));
    }
}