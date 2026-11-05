package com.example.todoapi.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.example.todoapi.common.ApiException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JwtTokenProvider만 따로 검사하는 단위 테스트. Spring을 띄우지 않으므로 빠르다.
 * 핵심: 위조·만료·형식 오류가 모두 같은 401 예외가 되는지 확인한다.
 */
class JwtTokenProviderTest {

	private static final String SECRET = "test-only-fake-jwt-secret-key-0123456789";
	private static final String OTHER_SECRET = "another-fake-jwt-secret-key-9876543210xx";

	private final JwtTokenProvider provider = new JwtTokenProvider(SECRET);

	@Test
	void validToken_returnsUserId() {
		String token = provider.createToken(5L);

		assertThat(provider.getUserId(token)).isEqualTo(5L);
	}

	@Test
	void tokenSignedWithOtherKey_isRejected() {
		// 비밀키를 모르는 사람이 자기 키로 서명해서 만든 위조 토큰
		String forged = new JwtTokenProvider(OTHER_SECRET).createToken(5L);

		assertUnauthorized(forged);
	}

	@Test
	void tamperedPayload_isRejected() {
		// 진짜 토큰의 본문(가운데 부분)만 다른 사용자 id로 바꿔치기한다. 서명이 맞지 않게 된다.
		String[] parts = provider.createToken(5L).split("\\.");
		String otherPayload = provider.createToken(999L).split("\\.")[1];
		String tampered = parts[0] + "." + otherPayload + "." + parts[2];

		assertUnauthorized(tampered);
	}

	@Test
	void expiredToken_isRejected() {
		// 올바른 키로 서명했지만 만료 시각이 1분 전인 토큰
		Instant now = Instant.now();
		String expired = Jwts.builder()
				.subject("5")
				.issuedAt(Date.from(now.minusSeconds(3600)))
				.expiration(Date.from(now.minusSeconds(60)))
				.signWith(key(SECRET), Jwts.SIG.HS256)
				.compact();

		assertUnauthorized(expired);
	}

	@Test
	void unsignedToken_isRejected() {
		// 서명 없이(alg=none) 만든 토큰
		String unsigned = Jwts.builder().subject("5").compact();

		assertUnauthorized(unsigned);
	}

	@Test
	void nonNumericSubject_isRejected() {
		String token = Jwts.builder()
				.subject("abc")
				.expiration(Date.from(Instant.now().plusSeconds(60)))
				.signWith(key(SECRET), Jwts.SIG.HS256)
				.compact();

		assertUnauthorized(token);
	}

	@Test
	void malformedOrEmptyToken_isRejected() {
		assertUnauthorized("abc");
		assertUnauthorized("a.b.c");
		assertUnauthorized("");
	}

	private void assertUnauthorized(String token) {
		assertThatThrownBy(() -> provider.getUserId(token))
				.isInstanceOf(ApiException.class)
				.hasMessage(JwtTokenProvider.UNAUTHORIZED_MESSAGE)
				.extracting(e -> ((ApiException) e).getStatus())
				.isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	private static SecretKey key(String secret) {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

}
