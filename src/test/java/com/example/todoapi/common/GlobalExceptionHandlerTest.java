package com.example.todoapi.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 아직 실제 API가 예외를 던지지 않으므로, 테스트 전용 컨트롤러로 각 오류 상황을 만들어 응답 모양을 검사한다.
 */
@WebMvcTest(HealthController.class) // 405 테스트에 /health가 필요하다. 다른 컨트롤러(서비스 의존)는 띄우지 않는다.
@Import(GlobalExceptionHandlerTest.ThrowingController.class)
class GlobalExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiException_usesItsStatusAndMessage() throws Exception {
		mockMvc.perform(get("/test/api-exception"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.error").value("Unauthorized"))
				.andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
	}

	@Test
	void brokenJson_returns400() throws Exception {
		mockMvc.perform(post("/test/body").contentType(MediaType.APPLICATION_JSON).content("{broken"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void unknownUrl_returns404() throws Exception {
		mockMvc.perform(get("/no-such-url"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void wrongMethod_returns405() throws Exception {
		mockMvc.perform(post("/health"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.status").value(405));
	}

	@Test
	void unexpectedException_returns500WithoutDetails() throws Exception {
		mockMvc.perform(get("/test/unexpected"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.status").value(500))
				.andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
	}

	@RestController
	static class ThrowingController {

		@GetMapping("/test/api-exception")
		String apiException() {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
		}

		@PostMapping("/test/body")
		String body(@RequestBody Map<String, String> body) {
			return "ok";
		}

		@GetMapping("/test/unexpected")
		String unexpected() {
			throw new IllegalStateException("내부 정보가 담긴 메시지");
		}

	}

}
