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
    document.getElementById('recipient-username').value = '';
}

// 친구추가모달 외부 클릭 시 모달 닫기
window.onclick = function(event) {
    const modal = document.getElementById('add-friend-modal');
    if (event.target === modal) {
        hideAddFriendModal();
    }
};

// 친구 요청 보내기
document.getElementById('send-request-btn').onclick = function() {
    const recipientUsername = document.getElementById('recipient-username').value.trim();

    if (!recipientUsername) {
        alert('이름(username)을 입력해 주세요.');
        return;
}

    // AJAX 요청 (컨트롤러로)
    fetch('/friends/request', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: recipientUsername
    })
    .then(response => {
        return response.text().then(text => {
            if (response.ok) {
                alert(text);
                hideAddFriendModal();
                fetchPendingRequests();
            } else {
                alert(text);
            }
        });
    })
    .catch(error => {
        console.error('오류 발생:', error);
        alert('오류가 발생했습니다. 나중에 다시 시도해 주세요.');
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
        li.innerHTML = `${request.recipientUsername}님에게 친구 요청을 보냈습니다. 상태: ${request.status}`;
        sentList.appendChild(li);
    });

    const receivedList = document.getElementById('received-requests');
    receivedList.innerHTML = ''; // 기존 내용을 초기화

    console.log(receivedRequests);

    receivedRequests.forEach(request => {

        const li = document.createElement('li');
        li.innerHTML = `${request.requesterUsername}님이 친구 요청을 보냈습니다.`;
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
             throw new Error('Failed to accept friend request');
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
              throw new Error('Failed to block friend request');
          }
          return response.text(); // 텍스트 응답을 받음
      })
      .then(message => {
          alert(message); // 차단 성공 메세지
          fetchPendingRequests(); // 친구 요청 목록 새로 고침
      })
      .catch(error => console.error('Error blocking friend request:', error));
  }

// 모든 친구 목록을 표시
function showAllFriends() {
    // 모든 섹션 숨기기
    hideAllSections();
    document.getElementById("all-friends-section").style.display = "block";

    // 친구 목록 가져오기
    fetchFriends();
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
            li.classList.add('friend-item');  // 클래스 추가
            li.innerHTML = `
                <div class="friend-bar">
                    <span class="friend-name">${friend.friendUsername}</span>
                    <button class="message-button" onclick="sendMessage('${friend.friendUsername}')">Message</button>
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

// 친구목록 필터링 함수
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

// 메시지 전송 함수
function sendMessage(friendUsername) {
    // 대화방 ID를 가져오는 API 요청
    fetch(`/messages/rooms?username=${friendUsername}`, {
        method: 'GET',
        headers: {
            [csrfHeader]: csrfToken // CSRF 토큰 추가
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('대화방을 가져오는 데 실패했습니다.'); // 사용자 친화적인 오류 메시지
        }
        return response.json(); // JSON 데이터를 반환받음
    })
    .then(data => {
        const roomId = data.id; // 대화방 ID
        // 메시지 전송을 위한 UI를 열거나 관련 로직을 추가
        openChatRoom(roomId); // 대화방 열기 함수 호출
    })
    .catch(error => {
        console.error('Error fetching room:', error);
        alert(error.message); // 사용자에게 오류 메시지 표시
    });
}

// 대화방 열기 함수
function openChatRoom(roomId) {
    // 대화방 UI를 열고, 해당 대화방의 메시지를 불러오는 로직 구현
    console.log(`Opening chat room with ID: ${roomId}`);
    // 추가 로직을 여기에 작성
}



// 페이지가 로드될 때 TAB 초기 설정
document.addEventListener("DOMContentLoaded", function() {
    // 초기 상태 설정
    hideAllSections(); // 모든 섹션 숨기기
    document.getElementById('pending-section').style.display = 'none'; // 기본적으로 대기 중 섹션은 보이지 않음

    // 탭 클릭 이벤트 설정
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', function() {
            const activeTabId = this.id;
            console.log("클릭된 탭 ID:", activeTabId); // 클릭된 탭 ID 확인

            hideAllSections(); // 모든 섹션 숨기기

            // 탭 클릭 분기처리
            if (activeTabId === 'pending-tab') {
                fetchPendingRequests(); // 대기 중 요청 가져옴
                document.getElementById('pending-section').style.display = 'block'; // 대기 중 섹션 표시
            } else if (activeTabId === 'friends-tab') {
                showAllFriends(); // 모든 친구 목록 보기
            } else if (activeTabId === 'blocked-tab') {
                document.getElementById('blocked-section').style.display = 'block'; // 차단 목록 섹션 표시
            }
        });
    });
});

// 모든 섹션 숨기는 함수
function hideAllSections() {
    document.querySelectorAll('.pending-section, .friends-section, .blocked-section').forEach(section => {
        section.style.display = 'none';
    });
}