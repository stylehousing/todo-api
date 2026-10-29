package com.example.todoapi.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 모든 컨트롤러에서 빠져나온 예외를 한곳에서 받아 ApiErrorResponse(JSON)로 바꾼다.
 * JSP 시절 web.xml의 <error-page> 설정과 비슷하지만, HTML 페이지 대신 JSON을 돌려준다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	// 우리가 직접 던진 예외: 담겨 있는 상태 코드와 메시지를 그대로 쓴다.
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(ApiException e) {
		return toResponse(e.getStatus(), e.getMessage());
	}

	// 요청 본문 JSON이 비었거나 문법이 깨진 경우.
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
		return toResponse(HttpStatus.BAD_REQUEST, "요청 본문(JSON) 형식이 올바르지 않습니다.");
	}

	// @Valid 검증에 실패한 경우: 어긴 규칙 중 첫 번째 메시지를 돌려준다.
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleNotValid(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(FieldError::getDefaultMessage)
				.orElse("입력값이 올바르지 않습니다.");
		return toResponse(HttpStatus.BAD_REQUEST, message);
	}

	// 그 밖의 모든 예외.
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleException(Exception e) {
		// 없는 URL(404), 허용하지 않는 메서드(405)처럼 Spring이 이미 상태 코드를 정해 둔 예외는 그 코드를 살린다.
		if (e instanceof ErrorResponse springError) {
			HttpStatus status = HttpStatus.valueOf(springError.getStatusCode().value());
			return toResponse(status, springError.getBody().getDetail());
		}
		// 정말 예상하지 못한 오류: 원인은 서버 로그에만 남기고, 클라이언트에는 내부 정보를 숨긴다.
		log.error("처리되지 않은 예외", e);
		return toResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
	}

	private ResponseEntity<ApiErrorResponse> toResponse(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(ApiErrorResponse.of(status, message));
	}

}
