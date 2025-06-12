package finalmission.member.auth;


import finalmission.member.domain.Role;
import finalmission.member.exception.JwtExtractException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

@Component
public class JwtTokenProvider {

    private final String secretKey;
    private final int expirationTime;

    public JwtTokenProvider(
        @Value("${secret.key}")String secretKey,
        @Value("${expiration.time}") int expirationTime) {
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
    }

    public String createToken(Long id, Role role) {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");

        return Jwts.builder()
            .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
            .setSubject(id.toString())
            .claim("role", role)
            .setIssuedAt(new Date())
            .signWith(key, HS256)
            .compact();
    }

    public Long getId(String token) {
        Claims claims = extractClaims(token);
        return Long.valueOf(claims.getId());
    }

    public Role getRole(String token) {
        Claims claims = extractClaims(token);
        return Role.valueOf(claims.get("role", String.class));
    }

    private Claims extractClaims(String token) throws JwtExtractException {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"))
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (MalformedJwtException malformedJwtException) {
            throw new JwtExtractException("유효한 토큰이 아닙니다.");
        } catch (ExpiredJwtException expiredJwtException) {
            throw new JwtExtractException("토큰이 만료되었습니다.");
        } catch (IllegalArgumentException argumentException) {
            throw new JwtExtractException("토큰이 비었습니다.");
        } catch (JwtException jwtException) {
            throw new JwtExtractException(jwtException.getMessage());
        }
    }
}
