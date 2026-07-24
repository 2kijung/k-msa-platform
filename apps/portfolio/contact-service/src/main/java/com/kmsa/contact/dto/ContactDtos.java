package com.kmsa.contact.dto;

/**
 * DTO 모음 — 문의 API의 입출력 계약(JSON).
 * [왜/MSA] 내부 엔티티(Contact)를 직접 노출하지 않고 DTO로 감싼다.
 */
public class ContactDtos {

    /** [입력] 문의 접수: { name, email, subject, message } */
    public record ContactRequest(String name, String email, String subject, String message) {}

    /** [출력] 표준 응답 래퍼 (auth-service와 같은 형태 규약을 각자 구현) */
    public record ApiResponse<T>(boolean success, String message, T data) {
        public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, "OK", data); }
        public static <T> ApiResponse<T> error(String m) { return new ApiResponse<>(false, m, null); }
    }
}
