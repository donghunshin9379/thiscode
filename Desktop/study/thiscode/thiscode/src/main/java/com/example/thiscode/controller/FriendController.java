package com.example.thiscode.controller;

import com.example.thiscode.domain.FriendList;
import com.example.thiscode.domain.FriendListDTO;
import com.example.thiscode.domain.FriendRequest;
import com.example.thiscode.service.FriendListService;
import com.example.thiscode.service.FriendRequestService;
import com.example.thiscode.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("friends")
public class FriendController {

    private final FriendRequestService friendRequestService;
    private final MemberService memberService;
    private final FriendListService friendListService;
    private static final Logger logger = LoggerFactory.getLogger(FriendController.class);

    public FriendController(FriendRequestService friendRequestService, MemberService memberService, FriendListService friendListService) {
        this.friendRequestService = friendRequestService;
        this.memberService = memberService;
        this.friendListService = friendListService;
    }

    @PostMapping("/request")
    public ResponseEntity<String> sendFriendRequest(@RequestParam(name = "recipientEmail") String recipientEmail) {
        String requesterEmail = memberService.getCurrentEmail(); // 현재 사용자 이메일 가져오기
        friendRequestService.sendFriendRequest(requesterEmail, recipientEmail);
        logger.info("요청자 : {}", requesterEmail);
        logger.info("수신자 : {}", recipientEmail);

        return ResponseEntity.ok("친구 요청을 보냈습니다.");
    }

    // 대기중인 친구 요청 조회
    @GetMapping("/pending")
    @ResponseBody
    public Map<String, Object> showPendingRequests() {
        String myEmail = memberService.getCurrentEmail(); // 현재 사용자 이메일 가져오기

        List<FriendRequest> sentRequests = friendRequestService.findPendingRequestsSent(myEmail);
        List<FriendRequest> receivedRequests = friendRequestService.findPendingRequestsReceived(myEmail);

        Map<String, Object> response = new HashMap<>();
        response.put("sentRequests", sentRequests);
        response.put("receivedRequests", receivedRequests);

        return response;
    }

    // 친구요청 수락
    @PostMapping("/accept")
    @ResponseBody
    public ResponseEntity<String> acceptFriendRequest(@RequestParam(name = "id") Long requestId) {
        logger.info("요청 id: {}", requestId);
        try {
            friendRequestService.acceptFriendRequest(requestId);
            return ResponseEntity.ok("친구요청을 수락했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수락 요청 에러");
        }
    }

    // 친구요청 차단
    @PostMapping("/block")
    @ResponseBody
    public ResponseEntity<String> blockFriendRequest(@RequestParam(name = "id") Long requestId) {
        logger.info("차단할 요청 id: {}", requestId);
        try {
            friendRequestService.blockFriendRequest(requestId);
            return ResponseEntity.ok("친구 요청이 차단되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("차단 요청 에러");
        }
    }
    
    // 친구목록 불러오기
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<List<FriendListDTO>> getFriends() {
        String userEmail = memberService.getCurrentEmail(); // 현재 사용자 이메일 가져오기

        // 요청자 기준으로 친구 목록 가져오기
        List<FriendList> myFriendsAsRequester = friendListService.getFriends(userEmail);

        // 수신자 기준으로 친구 목록 가져오기
        List<FriendList> myFriendsAsRecipient = friendListService.getFriendsByRecipient(userEmail);

        // 모든 친구 목록 통합
        List<FriendListDTO> allMyFriends = new ArrayList<>();

        // 요청자로서의 친구 목록 처리
        myFriendsAsRequester.forEach(friendList -> {
            FriendListDTO dto = new FriendListDTO();
            dto.setId(friendList.getId());
            dto.setUserEmail(friendList.getUser().getEmail());
            dto.setFriendEmail(friendList.getFriend().getEmail());
            allMyFriends.add(dto);
        });

        // 수신자로서의 친구 목록 처리
        myFriendsAsRecipient.forEach(friendList -> {
            FriendListDTO dto = new FriendListDTO();
            dto.setId(friendList.getId());
            dto.setUserEmail(friendList.getFriend().getEmail()); // 여기를 변경
            dto.setFriendEmail(friendList.getUser().getEmail()); // 여기를 변경
            allMyFriends.add(dto);
        });

        return ResponseEntity.ok(allMyFriends);
    }
}