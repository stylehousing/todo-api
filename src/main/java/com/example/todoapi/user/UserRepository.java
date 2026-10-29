package com.example.todoapi.user;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * users 테이블에 접근하는 DAO.
 * 인터페이스만 선언하면 Spring Data JPA가 구현 클래스를 자동으로 만들어 준다.
 * save(), findById() 같은 기본 메서드는 JpaRepository에 이미 들어 있다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	// 메서드 이름을 보고 "SELECT ... WHERE email = ?" 쿼리를 자동으로 만든다.
	boolean existsByEmail(String email);

}
