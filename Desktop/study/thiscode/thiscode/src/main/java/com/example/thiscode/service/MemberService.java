package com.example.thiscode.service;

import com.example.thiscode.controller.FriendController;
import com.example.thiscode.domain.CustomUserDetails;
import com.example.thiscode.domain.Member;
import com.example.thiscode.repository.SpringDataJpaMemberRepository;
import com.example.thiscode.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final SpringDataJpaMemberRepository springDataJpaMemberRepository;
    private static final Logger logger = LoggerFactory.getLogger(FriendController.class);

    public MemberService(SpringDataJpaMemberRepository springDataJpaMemberRepository) {
        this.springDataJpaMemberRepository = springDataJpaMemberRepository;
    }

    // 로그인 인증한 Email GET
    public String getCurrentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            logger.info("getCurrentEmail() {}",userDetails.getEmail());
            return userDetails.getEmail();
        }
        return null;
    }

    public Optional<Member> getUserInfo(String email) {
        // 이메일로 회원 정보를 조회 (비밀번호 제외)
        return springDataJpaMemberRepository.findUserInfoByEmail(email);
    }
    
    // 회원 가입 정보 저장
    public String save(Member member) {
        validateDuplicateMember(member);
        member.setPassword(PasswordUtil.encode(member.getPassword()));
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
        logger.info("findByEmail 검색: {}", email);
        return springDataJpaMemberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }




}
