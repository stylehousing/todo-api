package com.example.todoapi.auth;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT(로그인 토큰)를 만든다.
 * JSP 시절에는 로그인하면 서버 세션(session.setAttribute)에 사용자를 저장했지만,
 * 여기서는 서버가 서명한 토큰을 클라이언트에게 주고, 클라이언트가 요청마다 그 토큰을 보내게 한다.
 */
@Component
public class JwtTokenProvider {

	// 토큰 유효 시간. 지나면 다시 로그인해야 한다.
	public static final Duration EXPIRATION = Duration.ofHours(1);

	private final SecretKey key;

	// jwt.secret은 application.properties에서 환경 변수 JWT_SECRET 값으로 채워진다.
	public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
		// HS256 서명에는 32바이트(256비트) 이상의 키가 필요하다. 짧으면 WeakKeyException이 나서 서버 시작이 실패한다.
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String createToken(Long userId) {
		Instant now = Instant.now();
		// 토큰 본문은 누구나 Base64로 풀어 볼 수 있으므로 이메일 같은 개인정보는 넣지 않고 사용자 id(sub)만 담는다.
		// iat(발급 시각)과 exp(만료 시각)는 만료 처리에 필요한 표준 항목이다.
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(EXPIRATION)))
				.signWith(key, Jwts.SIG.HS256)
				.compact();
	}

}
