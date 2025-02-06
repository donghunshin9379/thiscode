let currentChatPartner;
let isChatUIActive = false;
// 채팅 UI 로드
function loadChatUI(friendEmail) {
    isChatUIActive = true;
    hideAllSections();
    showSection('chat-window');

    document.getElementById('chat-friend-email').textContent = friendEmail;

    // 이전 채팅 기록 로드
    loadChatHistory(friendEmail);

    // 현재 채팅 상대 설정
    currentChatPartner = friendEmail;
}
// 채팅방 ID 전역변수
let currentRoomId = null;

//채팅내역 로드
function loadChatHistory(friendEmail) {
    fetch(`/chat/history?friendEmail=${encodeURIComponent(friendEmail)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('채팅 기록 로드 실패: ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            const chatMessages = document.getElementById('chat-messages');
            chatMessages.innerHTML = '';

            const messages = data.messages || [];

            if (messages.length === 0) {
                console.log("채팅 기록이 없습니다.");
                return;
            }

            messages.forEach(message => {
                displayChatMessage(message.senderEmail, message.content, data.currentUserEmail, message.isRead);
            });
            console.log("##서버 ROOM ID : {}", data.roomId);
            // 변수 값 저장
            currentRoomId = data.roomId;

            enterRoom(currentRoomId);
        })
        .catch(error => {
            console.error('채팅 기록 로드 실패:', error);
        });
}

  function checkEnter(event) {
      if (event.key === 'Enter') { // 엔터키가 눌렸는지 확인
          event.preventDefault(); // 기본 동작 방지 (예: 폼 제출)
          sendMessage(); // 메시지 전송 함수 호출
      }
  }

    // 채팅내역 불러오기
    function displayChatMessage(sender, content, currentUserEmail) {
        const chatMessages = document.getElementById('chat-messages');
        const messageElement = document.createElement('div');

        if (sender === currentUserEmail) {
            messageElement.className = 'my-message';
            messageElement.textContent = `나: ${content}`;
        } else {
            messageElement.className = 'friend-message';
            messageElement.textContent = `${sender}: ${content}`;
        }

        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

 // 클라이언트 요청 TO 서버
 function sendMessage() {
     const messageInput = document.getElementById('message-content');
     const message = messageInput.value.trim();

     if (message && currentChatPartner) {
         socket.send(JSON.stringify({
             type: "sendMessage",
             payload: {
                 receiverEmail: currentChatPartner,
                 content: message
             }
         }));

//         // 서버로부터 응답을 받아 처리하기 위한 비동기 작업
//         socket.addEventListener('message', function onMessage(event) {
//             const response = JSON.parse(event.data);
//
//             if (response.type === 'messageSent') {
//                 displaySentMessage('나', message, response.payload.isRead, response.payload.messageId);
//                 socket.removeEventListener('message', onMessage); // 이벤트 리스너 제거
//             }
//         });

         messageInput.value = '';
     }
 }

// 서버 응답을 받아 메시지를 화면에 표시
function displaySentMessage(sender, content, isRead, messageId) {
    const chatMessages = document.getElementById('chat-messages');
    const messageElement = document.createElement('div');

    messageElement.className = 'my-message';
    messageElement.innerHTML = `나: ${content} <span class="read-status">${isRead ? '' : ' 1 '}</span>`;
    messageElement.setAttribute('data-message-id', messageId);

    chatMessages.appendChild(messageElement);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}








// 읽음처리 위한 채팅방 입장, 퇴장
//입장(웹소켓)
function enterRoom(roomId) {
        console.log("enterRoom 실행 ########### : ", roomId);
        socket.send(JSON.stringify({
            type: 'enterRoom',
            roomId: roomId
        }));
}


// 퇴장
function leaveRoom(roomId) {
    console.log("leaveRoom 실행 ########### : ", roomId);
    socket.send(JSON.stringify({
        type: 'leaveRoom',
        roomId: roomId
    }));
}