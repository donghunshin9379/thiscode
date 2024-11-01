package com.example.thiscode.controller;

import com.example.thiscode.domain.FriendList;
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
    public ResponseEntity<String> sendFriendRequest(@RequestBody String recipientUsername) {
        String requesterUsername = memberService.getCurrentUsername();
        friendRequestService.sendFriendRequest(requesterUsername, recipientUsername);
        return ResponseEntity.ok("친구 요청을 보냈습니다.");
    }


    // 대기중인 친구 요청 조회
    @GetMapping("/pending")
    @ResponseBody // JSON으로 반환하여 HTML전체를 렌더링 하는게 아닌, 필요한 데이터만 보내 렌더링함(서버부하 내려감)
    public Map<String, Object> showPendingRequests() {
        String myUsername = memberService.getCurrentUsername();

        List<FriendRequest> sentRequests = friendRequestService.findPendingRequestsSent(myUsername);
        List<FriendRequest> receivedRequests = friendRequestService.findPendingRequestsReceived(myUsername);

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
        try {
            friendRequestService.blockFriendRequest(requestId);
            return ResponseEntity.ok("친구요청을 거절했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("거절 요청 에러");
        }
    }

    // 모든 친구 목록 보기
    @GetMapping("/list")
    @ResponseBody //반환값 직렬화
    public ResponseEntity<List<FriendList>> getFriends() {
        String userUsername = memberService.getCurrentUsername();
        List<FriendList> myFriends = friendListService.getFriends(userUsername);
        return ResponseEntity.ok(myFriends);
    }
}
