package com.example.todoapi.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.example.todoapi.user.User;
import com.example.todoapi.user.UserRepository;
import com.jayway.jsonpath.JsonPath;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * POST /auth/login을 실제 DB와 연결한 상태에서 검사한다.
 * jwt.secret은 src/test/resources/config/application.properties의 테스트용 가짜 키를 쓴다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

	private static final String INVALID_CREDENTIALS = "이메일 또는 비밀번호가 올바르지 않습니다.";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	// 실제 BCrypt 동작은 그대로 두고, matches()가 몇 번 불렸는지만 확인하기 위해 감싼다.
	@MockitoSpyBean
	private PasswordEncoder passwordEncoder;

	@Value("${jwt.secret}")
	private String jwtSecret;

	private User user;

	@BeforeEach
	void setUp() {
		user = userRepository.save(new User("a@test.com", passwordEncoder.encode("password123")));
	}

	@Test
	void login_returnsTokenWithUserIdOnly() throws Exception {
		String body = login("a@test.com", "password123")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(3600))
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

		Claims claims = parse(JsonPath.read(body, "$.accessToken"));
		assertThat(claims.getSubject()).isEqualTo(String.valueOf(user.getId()));
		assertThat(claims.keySet()).containsExactlyInAnyOrder("sub", "iat", "exp");
		assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime()).isEqualTo(3600_000L);
	}

	@Test
	void login_ignoresEmailCase() throws Exception {
		login(" A@Test.com ", "password123")
				.andExpect(status().isOk());
	}

	@Test
	void wrongPassword_returns401() throws Exception {
		login("a@test.com", "wrongpass123")
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS))
				.andExpect(jsonPath("$.accessToken").doesNotExist());
	}

	@Test
	void unknownEmail_returnsSame401() throws Exception {
		login("nobody@test.com", "password123")
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
	}

	@Test
	void passwordOver72Bytes_returnsSame401EvenIfFirst72BytesMatch() throws Exception {
		// 한글 24자 = 72바이트(가입 가능한 최대 길이). BCrypt는 72바이트 뒤를 잘라 버리므로
		// 뒤에 글자를 덧붙인 비밀번호도 그대로 비교하면 통과된다. 이를 막는지 확인한다.
		String password = "가".repeat(24);
		userRepository.save(new User("long@test.com", passwordEncoder.encode(password)));

		login("long@test.com", password).andExpect(status().isOk());
		login("long@test.com", password + "x")
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
	}

	@Test
	void failures_stillRunBcryptOnceForSameResponseTime() throws Exception {
		login("nobody@test.com", "password123");
		login("a@test.com", "가".repeat(30));
		login("a@test.com", "wrongpass123");

		// 실패 이유와 상관없이 요청마다 BCrypt 비교를 정확히 한 번씩 한다.
		verify(passwordEncoder, times(3)).matches(anyString(), anyString());
	}

	@Test
	void blankEmail_returns400() throws Exception {
		login("", "password123")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("이메일은 필수입니다."));
	}

	@Test
	void missingPassword_returns400() throws Exception {
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"a@test.com\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("비밀번호는 필수입니다."));
	}

	private ResultActions login(String email, String password) throws Exception {
		String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
		return mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private Claims parse(String token) {
		return Jwts.parser()
				.verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

}
