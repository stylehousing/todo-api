package com.example.todoapi.common;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 서버가 살아 있는지 확인하는 용도의 API.
 * 서블릿으로 치면 "/health" URL에 매핑된 doGet() 하나짜리 서블릿이다.
 */
@RestController
public class HealthController {

	@GetMapping("/health")
	public Map<String, String> health() {
		// Map을 반환하면 Spring이 자동으로 JSON {"status":"ok"} 로 바꿔서 응답한다.
		return Map.of("status", "ok");
	}

}
