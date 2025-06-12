package finalmission.member.dto.response;

import finalmission.member.domain.Role;

public record LoginInfo(
    Long id,
    Role role
) {
}
