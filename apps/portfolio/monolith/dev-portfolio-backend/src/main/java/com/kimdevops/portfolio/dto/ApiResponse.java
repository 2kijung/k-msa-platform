package com.kimdevops.portfolio.dto;

/**
 * ApiResponse — 모든 REST 응답을 감싸는 표준 DTO.
 *
 * <p>구조: { success, message, data }
 * 모든 서비스가 동일한 응답 형태를 갖도록 규약. 단, 코드(JAR)를 공유하는 게 아니라
 * 각 서비스가 자기 스택으로 동일한 구조를 각자 구현한다 → 결합도 0.</p>
 *
 * @FROM  K-portfolio 원본 (k-msa 플랫폼 서비스들도 동일 구조를 각자 구현)
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Long timestamp;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
