let currentChatPartner;
// 채팅 UI 로드
function loadChatUI(friendEmail) {
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
                displayChatMessage(message.senderEmail, message.content, data.currentUserEmail);
            });
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

          displayChatMessage('나', message);
          messageInput.value = '';
      }
  }


  // 수신된 메시지를 화면에 표시
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
