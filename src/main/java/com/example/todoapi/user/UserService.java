package com.example.todoapi.user;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todoapi.common.ApiException;

/**
 * 회원 관련 비즈니스 로직. JSP 시절 Servlet 안에 섞여 있던 "중복 확인 → 암호화 → INSERT" 흐름을 여기로 모은다.
 */
@Service
public class UserService {

	// BCrypt는 비밀번호 앞 72바이트까지만 쓰고, 그보다 길면 예외를 던진다. (한글은 글자당 3바이트)
	private static final int BCRYPT_MAX_BYTES = 72;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// 생성자가 하나면 Spring이 알아서 필요한 Bean을 넣어 준다. (new UserDao() 를 직접 하지 않는다)
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		// "A@Test.com"과 "a@test.com"이 다른 계정으로 가입되지 않게 소문자로 맞춘다.
		String email = request.email().trim().toLowerCase(Locale.ROOT);

		if (request.password().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "비밀번호가 너무 깁니다.");
		}
		if (userRepository.existsByEmail(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
		}

		// 원문 비밀번호는 여기서만 잠깐 쓰고, DB에는 BCrypt 해시("$2a$10$...")만 저장한다.
		String encodedPassword = passwordEncoder.encode(request.password());
		User saved = userRepository.save(new User(email, encodedPassword));
		return SignupResponse.from(saved);
	}

}
