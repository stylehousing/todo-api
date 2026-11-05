package com.example.todoapi.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.todoapi.auth.AuthInterceptor;

/**
 * 인터셉터를 어떤 주소에 적용할지 정한다.
 * JSP 시절 web.xml의 <filter-mapping><url-pattern>에 해당한다.
 *
 * 기본은 "전부 검사"이고, 토큰 없이 써야 하는 주소만 열어 둔다.
 * 새 주소를 만들고 여기 등록을 깜빡해도 자동으로 잠겨 있으므로 안전하다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final AuthInterceptor authInterceptor;

	public WebConfig(AuthInterceptor authInterceptor) {
		this.authInterceptor = authInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor)
				.addPathPatterns("/**")
				.excludePathPatterns(
						"/health",      // 서버 상태 확인
						"/users",       // 회원가입 (정확히 /users만 열림. /users/xxx는 검사 대상)
						"/auth/login",  // 로그인
						"/error"        // Spring 내부 오류 처리 주소
				);
		// /h2-console은 Spring 컨트롤러가 아닌 별도 서블릿이라 이 인터셉터를 거치지 않는다.
	}

}
