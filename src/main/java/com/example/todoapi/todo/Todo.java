package com.example.todoapi.todo;

import java.time.LocalDateTime;

import com.example.todoapi.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 할 일 테이블(todos)과 대응하는 Entity.
 */
@Entity
@Table(name = "todos")
public class Todo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false)
	private boolean completed;

	// DAO 시절엔 int userId 필드를 두고 직접 JOIN 했지만, 여기서는 User 객체를 참조한다.
	// 실제 테이블에는 user_id 외래 키 컬럼이 생긴다.
	// LAZY: Todo를 조회할 때 User는 실제로 쓰는 순간에만 SELECT 한다.
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected Todo() {
	}

	public Todo(User user, String title) {
		this.user = user;
		this.title = title;
		this.completed = false;
		this.createdAt = LocalDateTime.now();
	}

	// setter 대신 의미 있는 이름의 메서드로 상태를 바꾼다.
	public void complete() {
		this.completed = true;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public boolean isCompleted() {
		return completed;
	}

	public User getUser() {
		return user;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

}
