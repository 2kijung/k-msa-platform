package com.kmsa.auth.config;

import com.kmsa.auth.entity.User;
import com.kmsa.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * ============================================================================
 * DataInitializer — 부팅 시 admin 계정 시드
 * ============================================================================
 * [무엇을] auth-service가 처음 뜰 때 admin 계정이 없으면 만들어 둔다.
 * [왜] 로그인 테스트를 하려면 계정이 하나는 있어야 한다. DB를 새로 띄워도(H2 인메모리)
 *      자동으로 admin이 생기게 해서 "띄우면 바로 로그인 가능"하게 한다.
 * [어떻게] CommandLineRunner = 앱 기동 완료 직후 1회 실행되는 훅.
 *          count()==0 (아무도 없을 때)만 삽입 → 여러 번 재시작해도 중복 생성 안 됨(멱등).
 * [보안] 비밀번호는 반드시 encode()로 해시해서 저장. (평문 저장 금지)
 *        운영에서는 이 기본 계정을 반드시 교체해야 함(@DEEP: 초기 비번 강제 변경 정책).
 * ============================================================================
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User(
                        "admin",
                        passwordEncoder.encode("admin123"),   // [보안] 해시로 저장
                        "admin@kmsa.local",
                        User.Role.ADMIN
                ));
                System.out.println("[auth-service] 기본 admin 계정 생성 완료 (admin / admin123)");
            }
        };
    }
}
