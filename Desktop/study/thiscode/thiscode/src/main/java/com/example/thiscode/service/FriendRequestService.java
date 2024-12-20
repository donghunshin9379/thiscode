package com.example.thiscode.service;

import com.example.thiscode.domain.FriendList;
import com.example.thiscode.domain.FriendRequest;
import com.example.thiscode.domain.FriendStatus;
import com.example.thiscode.domain.Member;
import com.example.thiscode.repository.FriendListRepository;
import com.example.thiscode.repository.FriendRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendListRepository friendListRepository;
    private final MemberService memberService;
    private static final Logger logger = LoggerFactory.getLogger(FriendRequestService.class);

    public FriendRequestService(FriendRequestRepository friendRequestRepository, FriendListRepository friendListRepository, MemberService memberService) {
        this.friendRequestRepository = friendRequestRepository;
        this.friendListRepository = friendListRepository;
        this.memberService = memberService;
    }

    @Transactional
    public void sendFriendRequest(String requesterEmail, String recipientEmail) {
        Member requester = memberService.findByEmail(requesterEmail);
        Member recipient = memberService.findByEmail(recipientEmail);

        if (requester == null || recipient == null) {
            throw new IllegalArgumentException("서비스레이어 : 사용자를 찾을 수 없습니다.");
        }

        // 요청자 중복 요청 방지
        List<FriendRequest> existingRequest = friendRequestRepository
                .findByRequesterEmailAndRecipientEmail(requesterEmail, recipientEmail);
        if (!existingRequest.isEmpty()) {
            throw new IllegalStateException("이미 친구요청을 보냈습니다.");
        }

        // 수신자가 이미 발신자에게 요청을 받은 경우 확인
        List<FriendRequest> existingReceivedRequest = friendRequestRepository
                .findByRecipientEmailAndRequesterEmail(requesterEmail, recipientEmail);

        if (!existingReceivedRequest.isEmpty()) {
            throw new IllegalStateException("해당 유저가 이미 요청을 보냈습니다.");
        }

        // 요청자와 수신자가 이미 ACCEPTED 상태 인지 확인
        List<FriendRequest> existingAcceptedFriend = friendRequestRepository
                .findByRequesterEmailAndRecipientEmailAndStatus(requesterEmail, recipientEmail, FriendStatus.ACCEPTED);

        if (!existingAcceptedFriend.isEmpty()) {
            throw new IllegalStateException("이미 친구로 등록된 유저입니다.");
        }

        // 친구 요청 생성 및 저장
        FriendRequest friendRequest = new FriendRequest(requesterEmail, recipientEmail);
        friendRequestRepository.save(friendRequest);
    }

    // 보낸 친구요청 목록 조회
    public List<FriendRequest> findPendingRequestsSent(String requesterEmail) {
        return friendRequestRepository.findByRequesterEmailAndStatus(requesterEmail, FriendStatus.PENDING);
    }

    // 받은 친구요청 목록 조회
    public List<FriendRequest> findPendingRequestsReceived(String recipientEmail) {
        return friendRequestRepository.findByRecipientEmailAndStatus(recipientEmail, FriendStatus.PENDING);
    }

    // 친구요청 수락
    @Transactional
    public void acceptFriendRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("수락할 친구 요청이 없습니다."));
        request.acceptRequest();
        friendRequestRepository.save(request); // 친구 요청 상태 저장
        FriendList friendList = new FriendList();
        // 요청자의 Member 객체
        Member requester = memberService.findByEmail(request.getRequesterEmail());
        // 수신자의 Member 객체
        Member recipient = memberService.findByEmail(request.getRecipientEmail());
        friendList.setUser(requester);  // 요청자
        friendList.setFriend(recipient); // 수신자
        friendListRepository.save(friendList); // 친구 목록 저장
    }

    // 친구요청 거절
    @Transactional
    public void blockFriendRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("차단할 친구 요청이 없습니다."));
        request.blockRequest();
        friendRequestRepository.save(request);
    }

    // 차단한 요청자 조회(이메일)
    public List<FriendRequest> findBlockedEmail(String recipientEmail) {
        return friendRequestRepository.findByRecipientEmailAndStatus(recipientEmail, FriendStatus.BLOCKED);
    }

}
