package com.kimdevops.portfolio.service;

import com.kimdevops.portfolio.entity.Profile;
import com.kimdevops.portfolio.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ProfileService — 모놀리스(K-portfolio)의 Profile CRUD 서비스.
 *
 * <p><b>Strangler Fig 전환 상태:</b> Phase 3에서 content-service로 분리 예정</p>
 * <p>이 클래스는 MSA 전환 중 잠시 공존하는 레거시 코드.
 * 게이트웨이가 해당 도메인의 라우트를 신규 서비스로 전환하면 이 코드는 사용되지 않는다.
 * 그때까지는 monolith가 fallback 역할을 수행한다.</p>
 *
 * @FROM  K-portfolio 원본 서비스 (Phase 0 흡수)
 * @RISK  이 서비스를 수정하면 신규 서비스와 로직이 달라질 수 있음 — 분리 완료 후 삭제 대상
 */
@Service
public class ProfileService {
    @Autowired
    private ProfileRepository profileRepository;

    /**
     * 현재 프로필 조회 (단일 행).
     * 없으면 빈 프로필을 하나 만들어 반환한다.
     */
    @Transactional
    public Profile getProfile() {
        List<Profile> all = profileRepository.findAll();
        if (!all.isEmpty()) {
            return all.get(0);
        }
        // 아직 없으면 기본 프로필 생성
        Profile profile = new Profile();
        profile.setName("이름을 입력하세요");
        return profileRepository.save(profile);
    }

    /**
     * 프로필 수정 (기존 단일 행을 덮어쓴다).
     */
    @Transactional
    public Profile updateProfile(Profile input) {
        Profile profile = getProfile(); // 기존(또는 새로 만든) 프로필

        profile.setName(input.getName());
        profile.setBirthDate(input.getBirthDate());
        profile.setLocation(input.getLocation());
        profile.setUniversity(input.getUniversity());
        profile.setMajor(input.getMajor());
        profile.setGraduationStatus(input.getGraduationStatus());
        profile.setMilitaryStatus(input.getMilitaryStatus());
        profile.setIntroduction(input.getIntroduction());
        profile.setEmail(input.getEmail());
        profile.setGithubUrl(input.getGithubUrl());
        profile.setBlogUrl(input.getBlogUrl());
        profile.setImageUrl(input.getImageUrl());
        profile.setCurrentStatus(input.getCurrentStatus());
        profile.setUpdatedAt(LocalDateTime.now());

        return profileRepository.save(profile);
    }
}
