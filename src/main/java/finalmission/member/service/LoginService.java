package finalmission.member.service;

import finalmission.member.domain.Member;
import finalmission.member.dto.request.LoginRequest;
import finalmission.member.dto.response.LoginInfo;
import finalmission.member.exception.LoginException;
import finalmission.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final MemberRepository memberRepository;

    public LoginService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public LoginInfo login(LoginRequest loginRequest) {
        Member loginMember = memberRepository.findByEmailAndPassword(loginRequest.email(), loginRequest.password())
            .orElseThrow(() -> new LoginException("로그인 정보가 일치하지 않습니다."));

        return new LoginInfo(loginMember.getId());
    }
}
