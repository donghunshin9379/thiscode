package com.example.thiscode.service;

import com.example.thiscode.domain.CustomUserDetails;
import com.example.thiscode.domain.Member;
import com.example.thiscode.repository.SpringDataJpaMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

//Security가 인증 처리하려면 DB에서 유저 정보를 UserDetails객체로 변환해야됌
//AuthenticationManger 인증과정에 현재 레이어 메소드가 호출되어 HTML에서 추출된 로그인 정보와 비교
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SpringDataJpaMemberRepository springDataJpaMemberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = springDataJpaMemberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("이메일을 찾을 수 없습니다: " + email));

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(member.getRole().name()));

        return new CustomUserDetails(member.getEmail(), member.getUsername(), member.getPassword(), authorities);
    }
}
