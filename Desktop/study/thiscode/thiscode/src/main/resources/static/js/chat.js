    let currentChatPartner;
    let isChatUIActive = false;
    // 채팅방 ID 전역변수
    let currentRoomId = null;

    // 채팅 UI 로드
    function loadChatUI(friendEmail) {
        isChatUIActive = true;
        console.log("채팅UI 활성화 상태 : {}", isChatUIActive);
        hideAllSections();
        showSection('chat-window');
        document.getElementById('chat-friend-email').textContent = friendEmail;
        // 이전 채팅 기록 로드
        loadChatHistory(friendEmail);
        // 현재 채팅 상대 설정
        currentChatPartner = friendEmail;
    }

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
                currentRoomId = data.roomId;
                enterRoom(currentRoomId);
            })
            .catch(error => {
                console.error('채팅 기록 로드 실패:', error);
            });
    }

    function checkEnter(event) {
      if (event.key === 'Enter') {
          event.preventDefault(); // 기본동작 방지
          sendMessage();
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

     // 서버에 sendMessage 요청
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
             messageInput.value = '';
         }
     }

    // web-socket.js "messageSent" 응답
    function displaySentMessage(sender, content, isRead, messageId) {
        const chatMessages = document.getElementById('chat-messages');
        const messageElement = document.createElement('div');

        messageElement.className = 'my-message';
        messageElement.innerHTML = `나: ${content} <span class="read-status">${isRead ? '' : ' 1 '}</span>`;
        messageElement.setAttribute('data-message-id', messageId);

        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // 서버에 enterRoom 요청
    function enterRoom(roomId) {
    socket.send(JSON.stringify({
        type: 'enterRoom',
        roomId: roomId
    }));
    }

    // 서버에 leaveRoom 요청
    function leaveRoom(roomId) {
    socket.send(JSON.stringify({
        type: 'leaveRoom',
        roomId: roomId
    }));
    }