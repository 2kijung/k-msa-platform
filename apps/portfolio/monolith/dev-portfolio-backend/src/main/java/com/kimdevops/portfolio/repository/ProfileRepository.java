package com.kimdevops.portfolio.repository;

import com.kimdevops.portfolio.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    // 프로필은 단일 행이므로 기본 findAll()/findById()로 충분
}
