package com.kmsa.auth.repository;

import com.kmsa.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * [무엇을] users 테이블 접근 리포지토리.
 * [어떻게] Spring Data JPA가 인터페이스만 보고 구현체를 자동 생성.
 *          findByUsername → "SELECT ... WHERE username = ?" 를 메서드 이름으로 유도.
 * [왜/MSA] 이 리포지토리는 auth-service 안에서만 존재한다. 다른 서비스는 이걸 import할 수 없다
 *          (모듈이 분리돼 컴파일 자체가 안 됨). MSA 경계가 코드로 강제되는 지점.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
