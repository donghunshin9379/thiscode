package com.example.thiscode.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendListDTO {
    private Long id;
    private String userEmail; // 요청자 이메일
    private String friendEmail; // 친구 이메일
}
