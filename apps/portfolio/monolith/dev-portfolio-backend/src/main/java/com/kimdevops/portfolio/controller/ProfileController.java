package com.kimdevops.portfolio.controller;

import com.kimdevops.portfolio.dto.ApiResponse;
import com.kimdevops.portfolio.entity.Profile;
import com.kimdevops.portfolio.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    /** 기본정보 조회 (공개) */
    @GetMapping
    public ResponseEntity<ApiResponse<Profile>> getProfile() {
        try {
            Profile profile = profileService.getProfile();
            return ResponseEntity.ok(ApiResponse.success(profile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /** 기본정보 수정 (관리자용 - 추후 인증 추가 예정) */
    @PutMapping
    public ResponseEntity<ApiResponse<Profile>> updateProfile(@RequestBody Profile request) {
        try {
            Profile updated = profileService.updateProfile(request);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
