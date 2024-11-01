package com.example.thiscode.service;

import com.example.thiscode.controller.FriendController;
import com.example.thiscode.domain.FriendRequest;
import com.example.thiscode.domain.FriendStatus;
import com.example.thiscode.domain.Member;
import com.example.thiscode.repository.FriendRequestRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final MemberService memberService;
    private final FriendListService friendListService;
    private static final Logger logger = LoggerFactory.getLogger(FriendController.class);

    public FriendRequestService(FriendRequestRepository friendRequestRepository, MemberService memberService, FriendListService friendListService) {
        this.friendRequestRepository = friendRequestRepository;
        this.memberService = memberService;
        this.friendListService = friendListService;
    }

    @Transactional
    public void sendFriendRequest(String requesterUsername, String recipientUsername) {
        // 요청자를 찾기
        Member requester = memberService.findByUsername(requesterUsername);
        // 수신자를 찾기
        Member recipient = memberService.findByUsername(recipientUsername);

        if (requester == null || recipient == null) {
            throw new IllegalArgumentException("서비스레이어 : 사용자를 찾을 수 없습니다.");
        }

        // 요청자가 보낸 요청이 있는 경우 확인
        List<FriendRequest> existingRequest = friendRequestRepository
                .findByRequesterUsernameAndRecipientUsername(requesterUsername, recipientUsername); // 나(요청자), 받을 사람
        if (!existingRequest.isEmpty()) {
            // 이미 보낸 요청이 있는 경우
            throw new IllegalStateException("이미 친구요청을 보냈습니다.");
        }

        // 요청자에게 이미 수신된 요청 확인
        List<FriendRequest> existingReceivedRequest = friendRequestRepository
                .findByRecipientUsernameAndRequesterUsername(requesterUsername,recipientUsername ); // 수신자, 요청자
        logger.info("요청자에게 이미 요청한 유저네임{}", recipientUsername);
        logger.info("현재 유저네임{}", requesterUsername);
        logger.info("수신된 요청 수: {}", existingReceivedRequest.size());

        if (!existingReceivedRequest.isEmpty()) {
            throw new IllegalStateException("해당 유저에게 이미 수신된 요청이 존재합니다.");
        }

        // 요청자와 수신자가 이미 ACCEPTED 상태로 친구인지 확인
        List<FriendRequest> existingAcceptedFriend = friendRequestRepository
                .findByRequesterUsernameAndRecipientUsernameAndStatus(requesterUsername, recipientUsername, FriendStatus.ACCEPTED);

        if (!existingAcceptedFriend.isEmpty()) {
            throw new IllegalStateException("이미 친구로 등록된 유저입니다.");
        }

        FriendRequest friendRequest = new FriendRequest(requesterUsername, recipientUsername);
        friendRequestRepository.save(friendRequest);
    }

    // 송신자 입장 친구요청 목록
    @Transactional
    public List<FriendRequest> findPendingRequestsSent(String requesterUsername) {
        return friendRequestRepository.findByRequesterUsernameAndStatus(requesterUsername, FriendStatus.PENDING);
    }

    // 수신자 입장 친구요청 목록
    @Transactional
    public List<FriendRequest> findPendingRequestsReceived(String recipientUsername) {
        return friendRequestRepository.findByRecipientUsernameAndStatus(recipientUsername, FriendStatus.PENDING);
    }

    // 친구 요청 수락
    @Transactional
    public void acceptFriendRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("수락할 친구 요청이 없습니다."));

        request.acceptRequest();
        friendRequestRepository.save(request);
        logger.info("{}님의 친구 요청을 수락했습니다.", request.getRequesterUsername());

        // 친구 목록에 추가
        friendListService.addFriend(request.getRequesterUsername(), request.getRecipientUsername());
    }

    // 친구 요청 차단
    @Transactional
    public void blockFriendRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("수락할 친구 요청이 없습니다."));

        request.blockRequest();
        friendRequestRepository.save(request);
        logger.info("{}님의 친구 요청을 거절했습니다.", request.getRequesterUsername());
    }

}
