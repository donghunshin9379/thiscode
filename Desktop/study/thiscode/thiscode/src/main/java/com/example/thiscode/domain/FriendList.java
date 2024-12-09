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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    private Member user; // 요청자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_email", referencedColumnName = "email", nullable = false)
    private Member friend; // 친구

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 기본 생성자
    public FriendList() {
        // 기본 생성자
    }

    // 생성자
    public FriendList(Member user, Member friend) {
        this.user = user;
        this.friend = friend;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendList that = (FriendList) o;
        return Objects.equals(user.getEmail(), that.user.getEmail()) &&
                Objects.equals(friend.getEmail(), that.friend.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user.getEmail(), friend.getEmail());
    }

    @Override
    public String toString() {
        return "FriendList{" +
                "user=" + (user != null ? user.getEmail() : "null") +
                ", friend=" + (friend != null ? friend.getEmail() : "null") +
                '}';
    }
}