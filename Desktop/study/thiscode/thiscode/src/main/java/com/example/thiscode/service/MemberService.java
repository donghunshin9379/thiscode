package com.example.thiscode.service;

import com.example.thiscode.controller.FriendController;
import com.example.thiscode.domain.CustomUserDetails;
import com.example.thiscode.domain.Member;
import com.example.thiscode.repository.SpringDataJpaMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final SpringDataJpaMemberRepository springDataJpaMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(FriendController.class);

    public MemberService(SpringDataJpaMemberRepository springDataJpaMemberRepository, PasswordEncoder passwordEncoder) {
        this.springDataJpaMemberRepository = springDataJpaMemberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 현재 사용자의 username 가져오기
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }
    
    // 회원 가입 정보 저장
    public String save(Member member) {
        validateDuplicateMember(member);
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        springDataJpaMemberRepository.save(member);
        return member.getEmail();
    }
    
    // 이메일 중복확인(submit 방지용)
    private void validateDuplicateMember(Member member) {
        springDataJpaMemberRepository.findByEmail(member.getEmail())
        .ifPresent(m -> {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        });
    }

    // 이메일 중복확인(유저 출력용)
    public boolean existsByEmail(String email) {
        return springDataJpaMemberRepository.findByEmail(email).isPresent();
    }


    // 전체회원 조회
    public List<Member> findMembers() {
        return springDataJpaMemberRepository.findAll();
    }

    public Member findByEmail(String email) {
        return springDataJpaMemberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    public Member findByUsername(String username) {
        logger.info("findByUsername 실행: {}", username);
        return springDataJpaMemberRepository.findByUsername(username)
        .orElseGet(() -> {
            logger.warn("사용자를 찾을 수 없습니다: {}", username);
            return null; // 사용자 정보를 찾지 못할 경우 null 반환
        });
    }

}
