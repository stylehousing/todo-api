package com.example.todoapi.auth;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.todoapi.common.ApiException;

import io.jsonwebtoken.JwtException;
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

	// 토큰이 없을 때, 위조됐을 때, 만료됐을 때 모두 이 메시지 하나로 응답한다.
	public static final String UNAUTHORIZED_MESSAGE = "로그인이 필요합니다.";

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

	/**
	 * 토큰의 서명과 만료 시각을 확인하고, 담겨 있는 사용자 id를 돌려준다.
	 * 위조(서명 불일치), 만료, 형식 오류 등 어떤 이유로 실패하든 같은 401 예외를 던진다.
	 * 실패 이유를 구분해 알려 주면 공격자에게 힌트가 되기 때문이다.
	 */
	public Long getUserId(String token) {
		try {
			String subject = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getSubject();
			return Long.valueOf(subject);
		} catch (JwtException | IllegalArgumentException e) {
			// ExpiredJwtException(만료), SignatureException(위조), MalformedJwtException(형식 오류)은 모두 JwtException의 자식이다.
			// IllegalArgumentException: 토큰이 빈 문자열이거나 sub가 숫자가 아닌 경우(NumberFormatException 포함).
			throw unauthorized();
		}
	}

	// 인증 실패 응답은 이 한 곳에서만 만든다. 인터셉터도 이 메서드를 써서 메시지를 통일한다.
	static ApiException unauthorized() {
		return new ApiException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED_MESSAGE);
	}

}
