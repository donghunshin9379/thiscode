package com.example.thiscode.repository;

import com.example.thiscode.domain.FriendRequest;
import com.example.thiscode.domain.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    // 내가 보낸 친구요청(상태)
    List<FriendRequest> findByRequesterEmailAndStatus(String requesterEmail, FriendStatus status);

    // 내가 받은 친구요청 (상태)
    List<FriendRequest> findByRecipientEmailAndStatus(String recipientEmail, FriendStatus status);

    // 요청자와 수신자 조회
    List<FriendRequest> findByRequesterEmailAndRecipientEmail(String requesterEmail, String recipientEmail);

    // 이미 친구 상태(ACCEPTED) 중복 조회
    List<FriendRequest> findByRequesterEmailAndRecipientEmailAndStatus(String requesterEmail, String recipientEmail, FriendStatus status);

    // 수신자입장  중복 요청 조회
    List<FriendRequest> findByRecipientEmailAndRequesterEmail(String recipientEmail, String requesterEmail);
}