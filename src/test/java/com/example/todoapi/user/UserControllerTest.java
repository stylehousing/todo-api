package com.example.todoapi.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * 컨트롤러~서비스~H2 DB까지 실제로 연결한 상태에서 POST /users를 검사한다.
 * @Transactional: 테스트가 끝나면 넣은 데이터를 롤백해서 다른 테스트에 영향을 주지 않는다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void signup_returns201WithoutPassword() throws Exception {
		signup("{\"email\":\"a@test.com\",\"password\":\"password123\"}")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.email").value("a@test.com"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void signup_storesBcryptHashNotRawPassword() throws Exception {
		signup("{\"email\":\"a@test.com\",\"password\":\"password123\"}");

		String stored = userRepository.findAll().get(0).getPassword();
		assertThat(stored).isNotEqualTo("password123");
		assertThat(stored).startsWith("$2a$");
		assertThat(passwordEncoder.matches("password123", stored)).isTrue();
	}

	@Test
	void duplicateEmail_returns409() throws Exception {
		signup("{\"email\":\"a@test.com\",\"password\":\"password123\"}");

		// 대소문자만 다른 이메일도 같은 계정으로 본다.
		signup("{\"email\":\"A@Test.com\",\"password\":\"otherpass123\"}")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
	}

	@Test
	void blankEmail_returns400() throws Exception {
		signup("{\"email\":\"\",\"password\":\"password123\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void invalidEmailFormat_returns400() throws Exception {
		signup("{\"email\":\"not-an-email\",\"password\":\"password123\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."));
	}

	@Test
	void shortPassword_returns400() throws Exception {
		signup("{\"email\":\"a@test.com\",\"password\":\"short\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 64자 이하여야 합니다."));
	}

	@Test
	void missingPassword_returns400() throws Exception {
		signup("{\"email\":\"a@test.com\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("비밀번호는 필수입니다."));
	}

	@Test
	void passwordOver72Bytes_returns400() throws Exception {
		// 한글 30자 = 90바이트. 글자 수 검증(64자)은 통과하지만 BCrypt 한도(72바이트)는 넘는다.
		String password = "가".repeat(30);
		signup("{\"email\":\"a@test.com\",\"password\":\"" + password + "\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("비밀번호가 너무 깁니다."));
	}

	private ResultActions signup(String json) throws Exception {
		return mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json));
	}

}
