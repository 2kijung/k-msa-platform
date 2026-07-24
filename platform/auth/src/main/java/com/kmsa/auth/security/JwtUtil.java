package com.kmsa.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * ============================================================================
 * JwtUtil — JWT 발급/검증 유틸
 * ============================================================================
 * [무엇을] 로그인 성공 시 토큰을 만들고(generate), 요청 시 토큰을 검증(parse)한다.
 * [왜/MSA] JWT = "무상태(stateless) 인증". 서버가 세션을 들고 있지 않으므로,
 *          auth-service가 여러 개로 복제(scale-out)돼도 어느 인스턴스가 받든 검증이 된다.
 *          (세션 방식이면 서비스 복제 시 세션 공유 문제가 생김 → MSA에 부적합)
 *
 * [원본 대비 개선] K-portfolio JwtUtil은:
 *   - role 클레임이 없어 "유효 토큰이면 무조건 ROLE_ADMIN"이 됐다(약점).
 *   - 검증 시 validate→isExpired→getUsername 으로 토큰을 3번 파싱했다(비효율).
 *   여기서는 (1) role을 토큰에 담고, (2) parse()로 한 번에 검증+클레임 추출한다.
 * ============================================================================
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /** [어떻게] 비밀키 문자열 → HMAC-SHA 서명키. 이 키로 서명하고, 같은 키로만 검증 가능. */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * [무엇을] username + role 을 담은 JWT 발급.
     * [왜] subject=username, claim "role"=권한. 이 role이 게이트웨이/하위 서비스로 전파돼
     *      "이 사용자가 뭘 할 수 있는지"를 토큰만 보고 판단하게 한다(DB 재조회 불필요).
     */
    public String generateToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * [무엇을] 토큰을 한 번 파싱해 검증 + 클레임 반환. 실패(위조/만료)면 예외를 던진다.
     * [왜] verify 엔드포인트가 이걸 호출해 "유효하면 사용자 정보 추출, 아니면 401"을 결정한다.
     *      한 번의 parseSignedClaims로 서명검증 + 만료검증이 동시에 처리됨(원본의 3번 파싱 개선).
     */
    public Claims parseAndValidate(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)   // 서명 불일치/만료면 여기서 JwtException
                .getPayload();
    }
}
