package com.example.todoapi.common;

import org.springframework.http.HttpStatus;

/**
 * 서비스 코드에서 "이 요청은 이런 상태 코드로 실패했다"를 알릴 때 던지는 예외.
 * 예: throw new ApiException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
 * 던지기만 하면 GlobalExceptionHandler가 받아서 JSON 오류 응답으로 바꿔 준다.
 */
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	public ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}

}
