package com.example.thiscode.service;

import com.example.thiscode.domain.FriendList;
import com.example.thiscode.repository.FriendListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FriendListService {

    private final FriendListRepository friendListRepository;

    @Autowired
    public FriendListService(FriendListRepository friendListRepository) {
        this.friendListRepository = friendListRepository;
    }

    // 특정 사용자의 친구 목록 가져오기 (요청자 기준)
    public List<FriendList> getFriends(String userEmail) {
        return friendListRepository.findByUserEmail(userEmail);
    }

    // 특정 사용자의 친구 목록 가져오기 (수신자 기준)
    public List<FriendList> getFriendsByRecipient(String userEmail) {
        return friendListRepository.findByFriendEmail(userEmail);
    }

    // 특정 사용자의 모든 친구 목록 가져오기
    public Set<String> getAllFriendEmails(String userEmail) {
        return friendListRepository.findAllFriendEmailsByUserEmail(userEmail);
    }

}
