package com.example.todoapi.auth;

/**
 * 로그인 성공 응답. 예: {"accessToken":"eyJ...","tokenType":"Bearer","expiresIn":3600}
 * 클라이언트는 이후 요청 헤더에 "Authorization: Bearer {accessToken}"을 붙여 보낸다.
 * expiresIn은 토큰 유효 시간(초)이다.
 */
public record LoginResponse(String accessToken, String tokenType, long expiresIn) {

	public static LoginResponse bearer(String accessToken, long expiresIn) {
		return new LoginResponse(accessToken, "Bearer", expiresIn);
	}

}
