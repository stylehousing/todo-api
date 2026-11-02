package com.example.todoapi.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 암호화 도구를 Spring Bean으로 하나 등록해 둔다.
 * 회원가입(암호화)과 로그인(비교) 양쪽에서 같은 객체를 주입받아 쓴다.
 */
@Configuration
public class PasswordConfig {

	// BCrypt는 비밀번호 앞 72바이트까지만 쓴다. (한글은 글자당 3바이트)
	// 가입할 때는 이보다 길면 거절하고, 로그인할 때는 잘린 앞부분만으로 통과되는 일이 없게 실패 처리한다.
	public static final int BCRYPT_MAX_BYTES = 72;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
