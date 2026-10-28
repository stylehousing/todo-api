package com.example.todoapi.common;

import org.springframework.http.HttpStatus;

/**
 * 모든 오류 응답이 공통으로 쓰는 JSON 모양.
 * 예: {"status":401,"error":"Unauthorized","message":"이메일 또는 비밀번호가 올바르지 않습니다."}
 */
public record ApiErrorResponse(int status, String error, String message) {

	public static ApiErrorResponse of(HttpStatus status, String message) {
		return new ApiErrorResponse(status.value(), status.getReasonPhrase(), message);
	}

}
