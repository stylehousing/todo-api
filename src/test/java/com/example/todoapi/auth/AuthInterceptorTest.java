package com.example.todoapi.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * 실제 서버 설정(WebConfig + AuthInterceptor + GlobalExceptionHandler)으로 요청을 보내 인증 검사를 확인한다.
 * 아직 토큰이 필요한 실제 주소(/todos)가 없으므로, 이 테스트 안에서만 쓰는 임시 주소 /test/protected를 만든다.
 * 이 주소는 테스트 코드에만 있어서 실제 서버에는 생기지 않는다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(AuthInterceptorTest.ProtectedController.class)
class AuthInterceptorTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Test
	void validToken_passesAndControllerReceivesUserId() throws Exception {
		String token = jwtTokenProvider.createToken(7L);

		getProtected("Bearer " + token)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(7));
	}

	@Test
	void noHeader_returns401() throws Exception {
		assertUnauthorized(mockMvc.perform(get("/test/protected")));
	}

	@Test
	void notBearerScheme_returns401() throws Exception {
		String token = jwtTokenProvider.createToken(7L);

		assertUnauthorized(getProtected(token));             // "Bearer " 없이 토큰만
		assertUnauthorized(getProtected("Basic " + token));  // 다른 방식
		assertUnauthorized(getProtected("Bearer "));         // 토큰이 비어 있음
	}

	@Test
	void forgedToken_returns401() throws Exception {
		String forged = new JwtTokenProvider("another-fake-jwt-secret-key-9876543210xx").createToken(7L);

		assertUnauthorized(getProtected("Bearer " + forged));
	}

	@Test
	void expiredToken_returns401() throws Exception {
		Instant now = Instant.now();
		String expired = Jwts.builder()
				.subject("7")
				.issuedAt(Date.from(now.minusSeconds(3600)))
				.expiration(Date.from(now.minusSeconds(60)))
				.signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
				.compact();

		assertUnauthorized(getProtected("Bearer " + expired));
	}

	@Test
	void unknownUrlWithoutToken_returns401() throws Exception {
		// 열어 두지 않은 주소는 존재 여부와 관계없이 먼저 토큰을 검사한다.
		assertUnauthorized(mockMvc.perform(get("/no-such-url")));
	}

	@Test
	void publicUrls_workWithoutToken() throws Exception {
		mockMvc.perform(get("/health"))
				.andExpect(status().isOk());

		// 401이 아니라 로그인 API 자체의 검증 결과(400)가 나오면 인터셉터를 통과한 것이다.
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	private ResultActions getProtected(String authorization) throws Exception {
		return mockMvc.perform(get("/test/protected").header(HttpHeaders.AUTHORIZATION, authorization));
	}

	private void assertUnauthorized(ResultActions result) throws Exception {
		result.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
	}

	// 테스트 전용 임시 주소. 7단계에서 /todos가 생기면 그쪽 테스트에서 실제 주소로 확인한다.
	@RestController
	static class ProtectedController {

		@GetMapping("/test/protected")
		Map<String, Long> protectedEndpoint(@RequestAttribute(AuthInterceptor.USER_ID) Long userId) {
			return Map.of("userId", userId);
		}

	}

}
