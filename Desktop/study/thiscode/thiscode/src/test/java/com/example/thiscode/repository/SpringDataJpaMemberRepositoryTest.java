package com.example.thiscode.repository;

import com.example.thiscode.domain.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class SpringDataJpaMemberRepositoryTest {
    @Autowired
    private SpringDataJpaMemberRepository springDataJpaMemberRepository;

   @Test
    public void testSpringDataJpaFindByEmail() {
        String email = "powershin97@icloud.com"; // 테스트할 이메일
        Optional<Member> member = springDataJpaMemberRepository.findByEmail(email);
        assertTrue(member.isPresent()); // 멤버가 존재하는지 확인
        System.out.println("Found member: " + member.get().getEmail());
    }

    @Test
    public void findByUsername() {
       String username = "gg";
       springDataJpaMemberRepository.findByUsername(username);
       System.out.println("findByUsername : " + username);
    }

}