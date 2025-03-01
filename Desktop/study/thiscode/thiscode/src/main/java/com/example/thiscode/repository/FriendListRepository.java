package com.example.thiscode.repository;

import com.example.thiscode.domain.FriendList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface FriendListRepository extends JpaRepository<FriendList, Long> {
    // 내 친구 목록 조회
    List<FriendList> findByUserEmail(String userEmail);

    // 친구 목록 중 내이메일 조회 (친구의 이메일 기준)
    List<FriendList> findByFriendEmail(String friendEmail);
    
    // 내 친구목록 중 친구 이메일 조회
    @Query("SELECT DISTINCT CASE WHEN fl.user.email = :userEmail THEN fl.friend.email ELSE fl.user.email END " +
            "FROM FriendList fl " +
            "WHERE fl.user.email = :userEmail OR fl.friend.email = :userEmail " +
            "AND (CASE WHEN fl.user.email = :userEmail THEN fl.friend.email ELSE fl.user.email END) != :userEmail")
    Set<String> findAllFriendEmailsByUserEmail(@Param("userEmail") String userEmail);
}