package com.example.thiscode.service;

import com.example.thiscode.domain.FriendList;
import com.example.thiscode.repository.FriendListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FriendListService {
    private final FriendListRepository friendListRepository;

    public FriendListService(FriendListRepository friendListRepository) {
        this.friendListRepository = friendListRepository;
    }

    @Transactional(readOnly = true)
    public List<FriendList> getFriends(String userUsername) {
        return friendListRepository.findByUserUsername(userUsername);
    }

    @Transactional
    public void addFriend(String userUsername, String friendUsername) {
        // 두 개의 친구 관계 추가 (양방향)
        FriendList friendList1 = new FriendList(userUsername, friendUsername);
        FriendList friendList2 = new FriendList(friendUsername, userUsername);

        friendListRepository.save(friendList1);
        friendListRepository.save(friendList2);
    }
}
