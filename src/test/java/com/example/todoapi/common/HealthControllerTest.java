package com.example.todoapi.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.todoapi.auth.JwtTokenProvider;

/**
 * 서버를 실제로 띄우지 않고 MockMvc로 GET /health 요청을 흉내 내서 응답을 검사한다.
 * 인증 인터셉터도 함께 켜 두고, 토큰 없이 /health가 열리는지 확인한다.
 * (@WebMvcTest는 인터셉터는 띄우지만 JwtTokenProvider는 띄우지 않으므로 직접 넣어 준다.)
 */
@WebMvcTest(HealthController.class)
@Import(JwtTokenProvider.class)
class HealthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void health_returnsOk() throws Exception {
		mockMvc.perform(get("/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ok"));
	}

}
