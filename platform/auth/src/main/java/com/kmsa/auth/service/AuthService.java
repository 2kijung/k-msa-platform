package com.kmsa.auth.service;

import com.kmsa.auth.dto.Dtos.*;
import com.kmsa.auth.entity.User;
import com.kmsa.auth.repository.UserRepository;
import com.kmsa.auth.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ============================================================================
 * AuthService — 로그인 비즈니스 로직
 * ============================================================================
 * [무엇을] username/password를 검증하고, 맞으면 JWT를 발급한다.
 * [어떻게] 1) username으로 사용자 조회
 *          2) 입력 password(평문)와 저장된 해시를 BCrypt로 대조
 *          3) 일치하면 role을 담은 JWT 발급
 * [왜/MSA] 이 로그인이 "MSA 전체 인증의 시작점"이다. 여기서 한 번 발급된 토큰으로
 *          이후 모든 서비스 요청이 인증된다(서비스마다 재로그인 불필요 = 단일 로그인).
 * ============================================================================
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // [왜] 생성자 주입 = 필드가 final로 고정되고 테스트 시 목 주입이 쉬움(@Autowired 필드주입보다 권장)
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 1) 사용자 조회 — 없으면 로그인 실패
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 2) 비밀번호 대조 — [보안] 저장은 해시라, 평문끼리 비교가 아니라 matches()로 검증
        //    [주의] 실패 메시지를 "비밀번호 틀림"으로 구체화하지 않는다 → 계정 존재 여부 노출 방지
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3) role을 실어 토큰 발급 → 이 토큰이 게이트웨이/하위 서비스로 전파됨
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        UserInfo info = new UserInfo(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
        return new LoginResponse(token, info);
    }
}
