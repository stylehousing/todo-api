package com.example.todoapi.auth;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todoapi.common.ApiException;
import com.example.todoapi.common.PasswordConfig;
import com.example.todoapi.user.User;
import com.example.todoapi.user.UserRepository;

/**
 * 로그인 처리: 이메일로 회원을 찾고, 비밀번호를 BCrypt로 비교한 뒤, 맞으면 JWT를 발급한다.
 */
@Service
public class AuthService {

	// 이메일이 틀렸는지 비밀번호가 틀렸는지 알려 주면 "가입된 이메일 목록"을 알아낼 수 있으므로 항상 같은 메시지를 쓴다.
	private static final String INVALID_CREDENTIALS = "이메일 또는 비밀번호가 올바르지 않습니다.";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	// 없는 이메일이어도 BCrypt 비교를 한 번 하기 위한 가짜 해시.
	// 비교를 건너뛰면 응답이 눈에 띄게 빨라져서, 시간만 재 봐도 가입된 이메일인지 알 수 있기 때문이다.
	// 서버가 뜰 때 같은 encoder(같은 cost)로 만들어서 진짜 해시와 비교 시간이 같게 한다.
	private final String dummyHash;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		Optional<User> user = userRepository.findByEmail(User.normalizeEmail(request.email()));

		// BCrypt는 72바이트 뒤를 잘라 버리므로, 72바이트 비밀번호 뒤에 아무 글자나 붙여도 통과될 수 있다. 그래서 실패로 본다.
		boolean tooLong = request.password().getBytes(StandardCharsets.UTF_8).length > PasswordConfig.BCRYPT_MAX_BYTES;

		// 어떤 경우든 BCrypt 비교를 정확히 한 번 해서 응답 시간을 맞춘다.
		String hash = (user.isPresent() && !tooLong) ? user.get().getPassword() : dummyHash;
		boolean matches = passwordEncoder.matches(request.password(), hash);

		if (user.isEmpty() || tooLong || !matches) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
		}

		String token = jwtTokenProvider.createToken(user.get().getId());
		return LoginResponse.bearer(token, JwtTokenProvider.EXPIRATION.toSeconds());
	}

}
