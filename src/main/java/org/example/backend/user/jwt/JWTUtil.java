package org.example.backend.user.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JWTUtil {

    private SecretKey secretKey;

    // secret key 객체 형성
    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // 토큰 요소 검증
    public String getLoginId(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("loginId", String.class);
    }

    // 토큰 요소 검증
    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    // 토큰 요소 검증 (토큰 유효 시간)
    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }




    // JWT 토큰 발급
    public String createJwt(String loginId, String role, int usersId, Long expiredMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (60 * 60 * 1000));
        return Jwts.builder().claim("loginId", loginId).claim("role", role).claim("usersId", usersId)//usersId 추가
                .issuedAt(now) // 현재 발행 시간
                .expiration(expiryDate).signWith(secretKey) // secretKey를 통해 암호화
                .compact();
    }

    // 토큰에서 usersId 추출
    public int getUsersId(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("usersId", Integer.class);
    }
}
