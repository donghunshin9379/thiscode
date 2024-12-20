package com.example.thiscode.repository;

import com.example.thiscode.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataJpaMemberRepository extends JpaRepository<Member, Long> {
    // 이메일로 회원 조회
    Optional<Member> findByEmail(String email);

    // 유저네임으로 회원 조회
    Optional<Member> findByUsername(String username);

    // 이메일로 회원정보 조회(암호화된 비밀번호 뜸)
    @Query("SELECT m FROM Member m WHERE m.email = :email")
    Optional<Member> findUserInfoByEmail(@Param("email") String email);

}