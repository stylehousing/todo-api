package com.example.todoapi.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 본문. 예: {"email":"a@test.com","password":"password123"}
 * 빈 값만 400으로 거른다. 형식·길이 규칙까지 검사하면 "그런 계정은 있을 수 없다"는 힌트를 주게 되므로,
 * 나머지는 모두 AuthService에서 똑같은 401로 처리한다.
 */
public record LoginRequest(

		@NotBlank(message = "이메일은 필수입니다.")
		String email,

		@NotBlank(message = "비밀번호는 필수입니다.")
		String password) {
}
