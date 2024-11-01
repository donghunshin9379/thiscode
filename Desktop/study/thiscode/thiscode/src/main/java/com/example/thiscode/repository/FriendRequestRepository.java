package com.example.thiscode.repository;

import com.example.thiscode.domain.FriendList;
import com.example.thiscode.domain.FriendRequest;
import com.example.thiscode.domain.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    // 보낸 친구요청
    List<FriendRequest> findByRequesterUsernameAndStatus(String requesterUsername, FriendStatus status);

    // 받은 친구요청
    List<FriendRequest> findByRecipientUsernameAndStatus(String recipientUsername, FriendStatus status);

    // 아래 두 메소드는 친구요청시 필요함.
    // 송신자 기준으로 요청을 찾는 메소드
    List<FriendRequest> findByRequesterUsernameAndRecipientUsername(String requesterUsername, String recipientUsername);

    // 요청자에게 수신된 요청을 찾는 메소드
    List<FriendRequest> findByRecipientUsernameAndRequesterUsername(String recipientUsername, String requesterUsername);

    // 이미 친구 상태(ACCEPTED)인 경우 조회
    List<FriendRequest> findByRequesterUsernameAndRecipientUsernameAndStatus(String requesterUsername, String recipientUsername, FriendStatus status);

}
