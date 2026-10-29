package com.example.todoapi.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * UC-1 회원가입 API.
 * 서블릿으로 치면 "/users"에 매핑된 doPost(). 요청 파싱·검증은 Spring이, 실제 처리는 UserService가 맡는다.
 */
@RestController
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	// @RequestBody: JSON 본문을 SignupRequest로 변환. @Valid: 변환 직후 검증 규칙을 검사하고, 어기면 400.
	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED) // 새 자원이 만들어졌으므로 200 대신 201.
	public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
		return userService.signup(request);
	}

}
