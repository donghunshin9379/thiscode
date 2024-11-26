package com.example.thiscode.repository;

import com.example.thiscode.domain.FriendRequest;
import com.example.thiscode.domain.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    // 보낸 친구요청 상태
    List<FriendRequest> findByRequesterEmailAndStatus(String requesterEmail, FriendStatus status);

    // 받은 친구요청 상태
    List<FriendRequest> findByRecipientEmailAndStatus(String recipientEmail, FriendStatus status);

    // 요청자와 수신자 확인
    List<FriendRequest> findByRequesterEmailAndRecipientEmail(String requesterEmail, String recipientEmail);

    // 이미 친구 상태(ACCEPTED)인 경우 조회
    List<FriendRequest> findByRequesterEmailAndRecipientEmailAndStatus(String requesterEmail, String recipientEmail, FriendStatus status);

    // 수신자가 발신자로부터 이미 요청을 받은 경우 확인
    List<FriendRequest> findByRecipientEmailAndRequesterEmail(String recipientEmail, String requesterEmail);
}