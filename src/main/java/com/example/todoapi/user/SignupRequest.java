package com.example.todoapi.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 본문. 예: {"email":"a@test.com","password":"password123"}
 * JSP 시절 request.getParameter()로 꺼낸 뒤 if문으로 검사하던 규칙을 어노테이션으로 적어 둔다.
 * 컨트롤러에서 @Valid를 붙이면 Spring이 메서드 실행 전에 이 규칙들을 검사한다.
 */
public record SignupRequest(

		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		@Size(max = 100, message = "이메일은 100자 이하여야 합니다.") // User.email 컬럼 길이와 맞춘다.
		String email,

		@NotBlank(message = "비밀번호는 필수입니다.")
		@Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
		String password) {
}
