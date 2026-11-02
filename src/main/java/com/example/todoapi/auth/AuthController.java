package com.example.todoapi.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * UC-2 로그인 API.
 * 성공하면 200과 함께 토큰을, 이메일·비밀번호가 틀리면 401, 빈 값이면 400을 돌려준다.
 */
@RestController
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	// 새 자원을 만드는 게 아니라 토큰을 받아 가는 요청이므로 기본값 200을 그대로 쓴다.
	@PostMapping("/auth/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

}
