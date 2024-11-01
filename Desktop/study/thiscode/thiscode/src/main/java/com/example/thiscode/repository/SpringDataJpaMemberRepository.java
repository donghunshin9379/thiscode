package com.example.thiscode.repository;

import com.example.thiscode.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataJpaMemberRepository extends JpaRepository<Member, Long> {

    // 이메일로 회원 찾기
    Optional<Member> findByEmail(String email);

    // 사용자 이름으로 회원 찾기
    Optional<Member> findByUsername(String username);
}