package com.example.todoapi.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 회원 테이블(users)과 1:1로 대응하는 Entity.
 * JSP 시절의 UserDTO와 비슷하지만, 이 클래스 자체가 테이블 정의 역할까지 한다.
 */
@Entity
@Table(name = "users") // USER는 H2 예약어라서 테이블 이름을 users로 지정한다.
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // DB의 AUTO_INCREMENT로 id를 만든다.
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	// 지금은 평문 컬럼만 만들어 둔다. 회원가입 단계에서 암호화한 값을 넣을 예정.
	@Column(nullable = false)
	private String password;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	// JPA가 DB에서 읽어 온 값을 채울 때 쓰는 기본 생성자. 외부에서는 쓰지 못하게 protected로 둔다.
	protected User() {
	}

	public User(String email, String password) {
		this.email = email;
		this.password = password;
		this.createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

}
