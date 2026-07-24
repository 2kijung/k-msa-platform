package com.kmsa.auth.dto;

/**
 * ============================================================================
 * DTO 모음 (요청/응답 데이터 계약)
 * ============================================================================
 * [무엇을] API의 입출력 형태를 정의한 record들.
 * [왜/MSA] MSA의 4원칙 = "계약(JSON)으로 통신". 이 DTO들이 곧 auth-service가
 *          외부와 주고받는 JSON 계약이다. 내부 엔티티(User)를 그대로 노출하지 않고
 *          DTO로 감싸 password 같은 민감 필드가 API로 새어나가지 않게 한다.
 * [어떻게] Java record = 불변 데이터 운반체. 생성자·getter·equals 자동 생성.
 * ============================================================================
 */
public class Dtos {

    /** [입력] 로그인 요청 바디: { "username": "...", "password": "..." } */
    public record LoginRequest(String username, String password) {}

    /** [출력] 로그인 응답: 발급된 토큰 + 사용자 최소 정보(password 제외) */
    public record LoginResponse(String token, UserInfo user) {}

    /** [출력] 노출해도 되는 사용자 정보만. (엔티티의 password/active 등은 뺌) */
    public record UserInfo(Long id, String username, String email, String role) {}

    /**
     * [출력] 표준 응답 래퍼. { success, message, data }
     * [왜] 모든 서비스가 같은 응답 형태를 쓰면 프론트/게이트웨이가 일관되게 처리 가능.
     *      (단, 이건 "형태 규약"을 공유하는 것이지 코드 JAR을 공유하는 게 아니다 —
     *       각 서비스가 자기 스택으로 같은 모양을 각자 구현. 그래서 결합도 0)
     */
    public record ApiResponse<T>(boolean success, String message, T data) {
        public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, "OK", data); }
        public static <T> ApiResponse<T> error(String message) { return new ApiResponse<>(false, message, null); }
    }
}
