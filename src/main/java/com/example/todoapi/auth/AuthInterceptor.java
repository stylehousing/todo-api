package com.example.todoapi.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 컨트롤러가 실행되기 직전에 로그인 토큰을 검사한다.
 * JSP 시절 LoginCheckFilter의 doFilter()와 같은 역할이다.
 *   - 세션의 loginUser 대신 "Authorization: Bearer <토큰>" 헤더를 확인하고,
 *   - 실패하면 login.jsp로 보내는 대신 401 JSON 응답을 돌려준다.
 * 어떤 주소에 적용할지는 common/WebConfig에서 정한다.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

	// 컨트롤러에서 @RequestAttribute(AuthInterceptor.USER_ID) Long userId 로 꺼내 쓴다.
	public static final String USER_ID = "userId";

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	public AuthInterceptor(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			// 여기서 던진 예외도 GlobalExceptionHandler가 받아서 다른 오류와 같은 JSON 형식으로 바꿔 준다.
			throw JwtTokenProvider.unauthorized();
		}

		Long userId = jwtTokenProvider.getUserId(header.substring(BEARER_PREFIX.length()));

		// JSP 시절 request.setAttribute()로 다음 페이지에 값을 넘기던 것과 같다.
		request.setAttribute(USER_ID, userId);
		// true = 통과 (필터의 chain.doFilter() 호출에 해당)
		return true;
	}

}
