package finalmission.member.dto.request;

public record LoginRequest(
    String email,
    String password
) {
}
