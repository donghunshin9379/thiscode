package com.example.thiscode.repository;

import com.example.thiscode.domain.FriendList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendListRepository extends JpaRepository<FriendList, String> {
    // 친구 목록 조회
    List<FriendList> findByUserUsername(String userUsername);

    // 친구 목록 table 저장
    FriendList save(FriendList friendList); 
}
