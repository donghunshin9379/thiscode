package com.example.thiscode.repository;

import com.example.thiscode.domain.FriendList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendListRepository extends JpaRepository<FriendList, Long> {
    // 특정 사용자의 친구 목록 조회
    List<FriendList> findByUserEmail(String userEmail);

    // 특정 사용자의 친구 목록 조회 (친구의 이메일 기준)
    List<FriendList> findByFriendEmail(String friendEmail);
    

}