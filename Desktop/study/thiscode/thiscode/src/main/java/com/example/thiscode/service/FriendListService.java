package com.example.thiscode.service;

import com.example.thiscode.domain.FriendList;
import com.example.thiscode.repository.FriendListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class FriendListService {

    private final FriendListRepository friendListRepository;

    @Autowired
    public FriendListService(FriendListRepository friendListRepository) {
        this.friendListRepository = friendListRepository;
    }

    // 내 친구 목록 조회 (요청자 기준)
    public List<FriendList> getFriends(String userEmail) {
        return friendListRepository.findByUserEmail(userEmail);
    }

    // 친구의 목록중 나 조회 (수신자 기준)
    public List<FriendList> getFriendsByRecipient(String userEmail) {
        return friendListRepository.findByFriendEmail(userEmail);
    }

    // 내 친구 목록 이메일 조회
    public Set<String> getAllFriendEmails(String userEmail) {
        return friendListRepository.findAllFriendEmailsByUserEmail(userEmail);
    }

}
