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


// 친구 목록 가져오기
function fetchFriends() {
    fetch('/friends/list', { // 친구 목록 요청
        method: 'GET',
        headers: {
            [csrfHeader]: csrfToken // CSRF 토큰 추가
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('친구 목록을 가져오는 데 실패했습니다.'); // 사용자 친화적인 오류 메시지
        }
        return response.json(); // JSON 데이터를 반환받음
    })
    .then(data => {
        displayFriends(data); // 친구 목록을 표시 함수 호출
    })
    .catch(error => {
        console.error('Error fetching friends:', error);
        alert(error.message); // 사용자에게 오류 메시지 표시
    });
}

// 친구 목록을 표시하는 함수
function displayFriends(friends) {
    const friendsList = document.getElementById('friends-list');
    friendsList.innerHTML = ''; // 기존 내용 초기화

    if (friends.length === 0) {
        friendsList.innerHTML = '<li>친구가 없습니다.</li>';
        return;
    }

    friends.forEach(friend => {
        const li = document.createElement('li');
        li.classList.add('friend-item');
        li.innerHTML = `
            <div class="friend-bar">
                <span class="friend-email">${friend.friendEmail}</span>
                <button class="message-button" onclick="sendMessage('${friend.friendEmail}')">Message</button>
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

// 로그아웃 함수
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

// 웹소켓 서버에 온라인 사용자 요청
function getOnlineUsers() {
    if (socket) { // socket이 정의되어 있는지 확인
        socket.send(JSON.stringify({ type: 'getOnlineUsers' }));
        console.log("온라인유저를 불러옵니다.")
    } else {
        console.error("Socket이 연결되지 않았습니다.");
    }
}

// 서버한테 받은 온라인 사용자 목록 표시
function displayOnlineUsers(users) {
    const userListContainer = document.getElementById('onlineUsersList'); // 사용자 목록을 표시할 HTML 요소
    const onlineSection = document.getElementById('online-section'); // 온라인 섹션

    // 기존 목록 초기화
    userListContainer.innerHTML = '';

    // 사용자 정보를 HTML로 추가
    users.forEach(user => {
        const userItem = document.createElement('li');
        userItem.textContent = user.email; // 사용자 이메일 또는 다른 정보를 표시
        userListContainer.appendChild(userItem);
    });

    // 온라인 섹션을 표시
    if (users.length > 0) {
        onlineSection.style.display = 'block'; // 사용자가 있을 경우 섹션 보이기
    } else {
        onlineSection.style.display = 'none'; // 사용자가 없을 경우 섹션 숨기기
    }

    console.log('온라인 사용자 목록이 업데이트되었습니다.');
}



// 상단 탭 설정
document.addEventListener("DOMContentLoaded", function() {
    hideAllSections(); // 모든 섹션 숨기기

    // 탭 클릭 이벤트 설정
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', function() {
            const activeTabId = this.id;
            console.log("클릭된 탭 ID:", activeTabId);

            hideAllSections();

            // 탭 클릭 분기처리
            if (activeTabId === 'pending-tab') {
                fetchPendingRequests();
                document.getElementById('pending-section').style.display = 'block';
            } else if (activeTabId === 'friends-tab') {
                fetchFriends();
                document.getElementById("all-friends-section").style.display = "block";
            } else if (activeTabId === 'blocked-tab') {
                document.getElementById('blocked-section').style.display = 'block';
            } else if (activeTabId === 'online-tab') {
                getOnlineUsers(); // 온라인 탭 클릭 시 사용자 목록 요청
                document.getElementById('online-section').style.display = 'block';
            }
        });
    });
});


// 모든 섹션 숨기는 함수
function hideAllSections() {
    document.querySelectorAll('.pending-section, .friends-section, .blocked-section, .online-section')
    .forEach(section => {
        section.style.display = 'none';
    });
}