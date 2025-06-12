package finalmission.member.service;

import finalmission.member.domain.Member;
import finalmission.member.dto.request.LoginRequest;
import finalmission.member.dto.response.LoginInfo;
import finalmission.member.exception.LoginException;
import finalmission.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LoginServiceTest {

    @Autowired
    private LoginService loginService;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 로그인_할_수_있다() {
        // given
        memberRepository.save(Member.createWithoutId("member1@email.com", "password"));
        LoginRequest loginRequest = new LoginRequest("member1@email.com", "password");

        // when
        LoginInfo loginResponse = loginService.login(loginRequest);

        // then
        assertThat(loginResponse.id()).isEqualTo(1L);
    }

    @Test
    void 잘못된_아이디_패스워드_입력시_로그인_할_수_없다() {
        // given
        LoginRequest noUser = new LoginRequest("noUser@email.com", "no");

        // when & then
        assertThatThrownBy(() -> loginService.login(noUser))
            .isInstanceOf(LoginException.class)
            .hasMessage("로그인 정보가 일치하지 않습니다.");
    }
}