package com.example.thiscode.repository;

import com.example.thiscode.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataJpaMemberRepository extends JpaRepository<Member, Long> {

    // 이메일로 회원 찾기
    Optional<Member> findByEmail(String email);

    // 사용자 이름으로 회원 찾기
    Optional<Member> findByUsername(String username);

    // 이메일로 유저정보 찾기(password 제외)
    @Query("SELECT m FROM Member m WHERE m.email = :email")
    Optional<Member> findUserInfoByEmail(@Param("email") String email);

}