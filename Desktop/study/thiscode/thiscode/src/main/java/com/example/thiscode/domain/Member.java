package com.example.thiscode.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

// DB 엔티티용
@Entity 
@Getter
@Setter
public class Member {

    @Id
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    private String nickname;
    private String username;
    @JsonIgnore // 이 어노테이션으로 비밀번호 필드를 JSON 직렬화에서 제외
    private String password;
    private LocalDate localDate;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;  // 기본값 USER 유지

}