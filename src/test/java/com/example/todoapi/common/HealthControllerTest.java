package com.example.todoapi.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 서버를 실제로 띄우지 않고 MockMvc로 GET /health 요청을 흉내 내서 응답을 검사한다.
 */
@WebMvcTest(HealthController.class)
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
