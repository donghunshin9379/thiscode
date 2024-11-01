package com.example.thiscode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "friend_list")
@Getter
@Setter
public class FriendList {
    
    //자동id값 생성
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_username", nullable = false)
    private String userUsername;

    @Column(name = "friend_username", nullable = false)
    private String friendUsername;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 기본 생성자
    public FriendList() {
        // 기본 생성자에서는 따로 시각 설정 필요 없음 (@PrePersist 사용)
    }

    // 생성자
    public FriendList(String userUsername, String friendUsername) {
        this.userUsername = userUsername;
        this.friendUsername = friendUsername;
    }

    // @PrePersist: 엔티티가 처음 저장되기 전에 호출되어 시각을 자동으로 설정
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // equals & hashCode: 객체 비교 시 id가 아닌 userUsername과 friendUsername으로 비교
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendList that = (FriendList) o;
        return Objects.equals(userUsername, that.userUsername) &&
                Objects.equals(friendUsername, that.friendUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userUsername, friendUsername);
    }

    @Override
    public String toString() {
        return "FriendList{" +
                "userUsername='" + userUsername + '\'' +
                ", friendUsername='" + friendUsername + '\'' + // 다른 속성이 있을 경우 추가
                '}';
    }
}
