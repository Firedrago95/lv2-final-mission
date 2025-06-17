package finalmission.running.service;

import finalmission.member.domain.Member;
import finalmission.member.domain.Role;
import finalmission.member.dto.response.LoginInfo;
import finalmission.member.exception.UnauthorizedException;
import finalmission.member.repository.MemberRepository;
import finalmission.running.dto.request.ReservationRequest;
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
}