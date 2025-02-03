// CSRF 토큰을 GET
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');


// 친구추가 모달 함수
function showAddFriendModal() {
    document.getElementById('add-friend-modal').style.display = 'block';
}

// 친구추가 모달 닫는 함수
function hideAddFriendModal() {
    document.getElementById('add-friend-modal').style.display = 'none';
    // 모달을 닫을 때 입력 필드 초기화
    document.getElementById('recipient-email').value = '';
}

// 친구추가모달 외부 클릭 시 모달 닫기
window.onclick = function(event) {
    const modal = document.getElementById('add-friend-modal');
    if (event.target === modal) {
        hideAddFriendModal();
    }
};

 // 친구 요청 보내기 함수
    function sendFriendRequest() {
        const email = document.getElementById('recipient-email').value;

        // AJAX 요청 등을 통해 서버에 친구 요청 전송
        // 예시:
        fetch('/friends/request', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ recipientEmail: email })
        })
        .then(response => {
            if (response.ok) {
                alert('친구 요청이 전송되었습니다.');
                // 모달 닫기 및 입력 필드 초기화
                document.getElementById('add-friend-modal').style.display = 'none';
                document.getElementById('recipient-email').value = '';
            } else {
                alert('친구 요청 전송에 실패했습니다.');
            }
        })
        .catch(error => console.error('Error:', error));
    }


// 친구 요청 보내기
document.getElementById('send-request-btn').onclick = function() {
    const recipientEmail = document.getElementById('recipient-email').value.trim(); // 이메일 입력값 가져오기

    if (!recipientEmail) {
        alert('이메일을 입력해 주세요.'); // 이메일 입력 확인
        return;
    }

    // AJAX 요청 (컨트롤러로)
    fetch(`/friends/request?recipientEmail=${encodeURIComponent(recipientEmail)}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
        return response.text().then(text => {
            if (response.ok) {
                alert(text); // 성공 메시지 표시
                hideAddFriendModal(); // 모달 닫기
                fetchPendingRequests(); // 대기 중인 친구 요청 새로 고침
            } else {
                alert(text); // 오류 메시지 표시
            }
        });
    })
    .catch(error => {
        console.error('오류 발생:', error); // 콘솔에 오류 로그 출력
        alert('오류가 발생했습니다. 나중에 다시 시도해 주세요.'); // 사용자에게 오류 알림
    });
};

// 대기 중 친구 요청을 GET
function fetchPendingRequests() {
    fetch('/friends/pending', {
        method: 'GET', // GET 요청
        headers: {
            [csrfHeader]: csrfToken // CSRF 토큰 추가
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch pending requests');
        }
        //상황에 따라 서버에서 쏘는 데이터 형태 변경 가능
        //return response.text();
        return response.json(); // JSON 데이터를 반환받음
    })
    .then(data => {
        displayPendingRequests(data.sentRequests, data.receivedRequests); // 요청 데이터를 표시
    })
    .catch(error => console.error('Error fetching pending requests:', error));
}

// GET한 대기중 친구 요청 표시
function displayPendingRequests(sentRequests, receivedRequests) {
    const sentList = document.getElementById('sent-requests');
    sentList.innerHTML = ''; // 기존 내용을 초기화

    console.log(sentRequests);

    sentRequests.forEach(request => {
        const li = document.createElement('li');
        li.innerHTML = `${request.recipientEmail}님에게 친구 요청을 보냈습니다. 상태: ${request.status}`; // username에서 email로 변경
        sentList.appendChild(li);
    });

    const receivedList = document.getElementById('received-requests');
    receivedList.innerHTML = ''; // 기존 내용을 초기화

    console.log(receivedRequests);

    receivedRequests.forEach(request => {
        const li = document.createElement('li');
        li.innerHTML = `${request.requesterEmail}님이 친구 요청을 보냈습니다.`; // username에서 email로 변경
        li.innerHTML += `<input type="hidden" value="${request.id}" name="id">`;
        li.innerHTML += `<button type="button" class="accept" onclick="acceptRequest(${request.id})">수락</button>`;
        li.innerHTML += `<button type="button" class="block" onclick="blockRequest(${request.id})">차단</button>`;
        receivedList.appendChild(li);
    });

    document.getElementById('pending-section').style.display = 'block'; // 섹션 표시
}

// 친구요청 수락 함수
function acceptRequest(requestId) {
    fetch(`/friends/accept?id=${requestId}`, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken // CSRF 토큰 추가
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('친구 요청 수락에 실패했습니다.');
        }
        return response.text();
    })
    .then(message => {
        alert(message);
        fetchPendingRequests(); // 친구 요청 목록 새로 고침
    })
    .catch(error => console.error('Error accepting friend request:', error));
}

// 친구요청 차단 함수
function blockRequest(requestId) {
    fetch(`/friends/block?id=${requestId}`, {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('친구 요청 차단에 실패했습니다.');
        }
        return response.text(); // 텍스트 응답을 받음
    })
    .then(message => {
        alert(message); // 차단 성공 메세지
        fetchPendingRequests(); // 친구 요청 목록 새로 고침
    })
    .catch(error => console.error('Error blocking friend request:', error));
}


// 모두 (친구목록)탭 GET
function fetchFriends() {
    fetch('/friends/list', {
        method: 'GET',
        headers: {
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('친구 목록을 가져오는 데 실패했습니다.'); // 사용자 친화적인 오류 메시지
        }
        return response.json();
    })
    .then(data => {
        displayFriends(data);
    })
    .catch(error => {
        alert(error.message);
    });
}

// GET 한 친구목록 표시 (파라미터 DTO객체 )
function displayFriends(friends) {
    const friendsList = document.getElementById('friends-list');
    friendsList.innerHTML = '';

    if (friends.length === 0) {
        friendsList.innerHTML = '<li>친구가 없습니다.</li>';
        return;
    }

    friends.forEach(friend => {
        const li = document.createElement('li');
        li.classList.add('friend-item');
        li.innerHTML = `
            <div class="friend-bar" onclick="loadChatUI('${friend.friendEmail}')">
              <span class="friend-email">${friend.friendEmail}</span>
            </div>
        `;
        friendsList.appendChild(li);
    });

    // 친구 목록이 화면에 표시된 후, 현재 검색어에 맞게 필터링
    const searchTerm = document.getElementById("search-input").value.toLowerCase();
    filterFriends(searchTerm);
}


// 친구 검색 이벤트
document.getElementById("search-input").addEventListener("input", function() {
    const searchTerm = this.value.toLowerCase();
    filterFriends(searchTerm);
});

// 친구목록 필터링 함수 (email로 수정필요)
function filterFriends(searchTerm) {
    const friendsListItems = document.querySelectorAll("#friends-list li");

    friendsListItems.forEach(friend => {
        const friendName = friend.textContent.toLowerCase();
        if (friendName.includes(searchTerm)) {
            friend.style.display = "block"; // 검색어와 일치하면 표시
        } else {
            friend.style.display = "none"; // 일치하지 않으면 숨김
        }
    });
}

// 로그아웃 탭
function logout() {
    // CSRF 토큰 가져오기
    var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // POST 요청
    fetch('/logout', {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        },
        credentials: 'same-origin'
    }).then(response => {
        if (response.ok) {
            // 로그아웃 성공 시
            window.location.href = '/login';
        } else {
            console.error('로그아웃 실패');
        }
    }).catch(error => {
        console.error('로그아웃 중 오류 발생:', error);
    });
}

//차단목록 탭
function loadBlockedUsers() {
    fetch('/friends/block-list')
        .then(response => response.json())
        .then(blockedEmails => {
            const blockedList = document.getElementById('block-list');
            blockedList.innerHTML = ''; // 기존 목록 초기화
            blockedEmails.forEach(email => {
                const li = document.createElement('li');
                li.innerHTML = `
                    <span>${email}</span>
                    <form action="/friends/unblock" method="post" style="display:inline;">
                        <input type="hidden" value="${email}" name="email"/>
                        <button type="submit">차단 해제</button>
                    </form>
                `;
                blockedList.appendChild(li);
            });
            document.getElementById('blocked-section').style.display = 'block';
        })
        .catch(error => console.error('Error:', error));
}

// 온라인 탭 (to 서버 CustomWebSocketController)
function fetchOnlineFriends() {
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({ type: "onlineFriends" }));
    } else {
        console.warn("WebSocket 연결이 아직 열리지 않았습니다. 연결을 기다립니다.");
        setTimeout(fetchOnlineFriends, 100); // 100ms 후에 다시 시도
    }
}
// 활성화 탭 추적 변수
let activeTabId = '';
let currentUserEmail;

// 상단 탭 설정
document.addEventListener("DOMContentLoaded", function() {
    initializeFriendList();
    hideAllSections(); // 모든 섹션 숨기기

    // 탭 클릭 이벤트 설정
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', function() {
            activeTabId = this.id;  // 'const' 대신 'let' 사용
            console.log("클릭된 탭 ID:", activeTabId);
            hideAllSections(); // 모든 섹션 숨기기
            handleTabClick(activeTabId); // 선택한 탭에 해당하는 섹션 표시
            document.getElementById('chat-window').style.display = 'none'; // 채팅창 숨김
        });
            });
        });

function initializeFriendList() {
    // 기본적으로 온라인 탭을 활성화한 것처럼 동작
    handleTabClick('online-tab');
}

// 탭 클릭 처리 함수
function handleTabClick(tabId) {
    activeTabId = tabId;
    hideAllSections();

    switch (tabId) {
        case 'pending-tab':
            fetchPendingRequests();
            showSection('pending-section');
            closeChatUI();
            break;
        case 'friends-tab':
            fetchFriends();
            showSection('all-friends-section');
            closeChatUI();
            break;
        case 'blocked-tab':
            loadBlockedUsers();
            showSection('blocked-section');
            closeChatUI();
            break;
        case 'online-tab':
            fetchOnlineFriends();
            showSection('online-section');
            closeChatUI();
            break;
        default:
            console.warn("알 수 없는 탭 ID:", tabId);
    }
}

// 특정 섹션 표시하기
function showSection(sectionId) {
    const section = document.getElementById(sectionId);
    if (section) {
        section.style.display = 'block';  // 해당 섹션을 보이게 함
    }
}

// 모든 섹션 숨기는 함수
function hideAllSections() {
    document.querySelectorAll('.pending-section, .friends-section, .blocked-section, .online-section, #chat-window')
        .forEach(section => {
            section.style.display = 'none';
        });
}

// 채팅 UI를 닫는 함수
function closeChatUI() {
    console.log("closeChatUI() 실행#######");
    if (currentRoomId) {
        leaveRoom(currentRoomId);
    }
    isChatUIActive = false;
    currentChatPartner = null;
    currentRoomId = null;
}

document.addEventListener('DOMContentLoaded', function() {
    fetch('/chat/dm-list')
        .then(response => response.json())
        .then(chatFriends => {
            const chatList = document.getElementById('direct-messages-list');
            chatFriends.forEach(friendEmail => {
                const li = document.createElement('li');
                li.innerHTML = `
                    <div class="chat-item" onclick="loadChatUI('${friendEmail}')">
                        <span>${friendEmail}</span>
                    </div>
                `;
                chatList.appendChild(li);
            });
        })
        .catch(error => console.error('Error:', error));
});

//// 채팅 읽음 로직
//
//// 채팅방 마지막메세지 추출 (roomId로 해당 채팅방 마지막 메세지 추출)
//function getLastReadMessageId(roomId) {
//    fetch(`/chat/last?roomId=${encodeURIComponent(roomId)}`, {
//        method: 'GET', // GET 요청으로 변경
//        headers: {
//            'Content-Type': 'application/json',
//        }
//    })
//    .then(response => {
//        if (!response.ok) {
//            throw new Error('Network response was not ok');
//        }
//        return response.json();
//    })
//    .then(lastReadMessageId => {
//        console.log("LastReadMesageID : ", lastReadMessageId);
//    })
//    .catch(error => {
//        console.error('getLastReadMessageId 요청 중 오류 발생:', error);
//    });
//}
//
//

