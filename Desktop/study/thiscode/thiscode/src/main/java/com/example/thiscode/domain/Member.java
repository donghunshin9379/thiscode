package com.example.thiscode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;


@Entity  //이제 JPA가 관리함
@Getter
@Setter
public class Member {

    @Id
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    private String nickname;
    private String username;
    private String password;
    private LocalDate localDate;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;  // 기본값 USER 유지

}