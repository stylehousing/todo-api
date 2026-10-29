package com.example.todoapi.user;

import java.time.LocalDateTime;

/**
 * 회원가입 성공 응답. Entity를 그대로 돌려주면 password 필드까지 JSON에 실리므로, 보여줄 값만 골라 담는다.
 */
public record SignupResponse(Long id, String email, LocalDateTime createdAt) {

	public static SignupResponse from(User user) {
		return new SignupResponse(user.getId(), user.getEmail(), user.getCreatedAt());
	}

}
