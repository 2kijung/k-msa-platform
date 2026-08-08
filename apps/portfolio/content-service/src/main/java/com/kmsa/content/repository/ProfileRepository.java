package com.kmsa.content.repository;

import com.kmsa.content.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * ProfileRepository — portfolio 스키마 profiles 테이블 (단일 행 패턴).
 * [MSA] content-service만 소유. 프로필은 1개만 존재하므로 findTopByOrderByIdAsc()로 첫 행 조회.
 * [직관화] findFirst()와 동일하지만 명시적 정렬(idAsc)로 항상 동일한 행 반환 보장.
 * @FROM  K-portfolio ProfileRepository
 * @PHASE:3
 */
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findTopByOrderByIdAsc();
}
