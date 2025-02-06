let socket;
// 웹 소켓
window.addEventListener('load', connectWebSocket);

//1
function connectWebSocket() {
    socket = new WebSocket("ws://localhost:8080/ws"); //WebSocketConfig에 CustomWebSocketHandler가 요청 처리

    socket.onopen = function(event) {
        console.log("WebSocket 오픈.");
    };

   //서버가 보낸 메시지 수신 및 처리 함수
   socket.onmessage = function(event) {
       try {
           const response = JSON.parse(event.data);
           console.log("서버 메시지:", response);

           switch(response.type) {
               case "statusUpdate":
                   updateUserStatus(response.payload.email, response.payload.status);
                   fetchOnlineFriends();
                   break;
               case "onlineFriends":
                   displayOnlineFriends(response.friends);
                   document.getElementById('online-section').style.display = 'block'; // 섹션 표시
                   break;
               case "displayMessage":
                   displayChatMessage(response.payload.senderEmail, response.payload.content);
                   break;
               case "readStatusUpdate":
                   updateReadStatus(response.payload.messageId, response.payload.isRead);
                   break;
               case "messageSent":
                   displaySentMessage('나', response.payload.content, response.payload.isRead, response.payload.messageId);
                   break;
                case "notification":
                    // 알림 처리 로직
                    displayNotification(response.payload.senderEmail, response.payload.content);
                    break;
               default:
                   console.warn("알 수 없는 요청:", response.type);
           }
       } catch (error) {
           console.error("메세지 수신 요청 에러:", error);
       }
   };

   // 온라인친구 리스트 + 채팅UI 로드
   function displayOnlineFriends(onlineFriends) {
       const onlineFriendsList = document.getElementById('onlineUsersList');
       onlineFriendsList.innerHTML = '';

       if (onlineFriends.length === 0) {
           onlineFriendsList.innerHTML = '<li>온라인 친구가 없습니다.</li>';
           return;
       }

       onlineFriends.forEach(friend => {
           const li = document.createElement('li');
           li.classList.add('online-friend-item');
           li.innerHTML = `
               <div class="online-friend-bar" onclick="loadChatUI('${friend}')">
                   <span class="friend-email">${friend}</span>
               </div>
           `;
           onlineFriendsList.appendChild(li);
       });
   }


function updateUserStatus(email, status) {
    // 상태 변경 처리
    const onlineUsersList = document.getElementById('onlineUsersList');

    if (status === 'online') {
        // 친구가 온라인일 때
        if (!document.getElementById(`user-${email}`)) {
            const userElement = document.createElement('li');
            userElement.id = `user-${email}`;
            userElement.textContent = `${email} 온라인`;
            onlineUsersList.appendChild(userElement);
            console.log(`User ${email} 유저 온라인`);

            // 클릭 시 채팅 UI 로드
            userElement.addEventListener('click', () => loadChatUI(email));
        }
    } else {
        // 친구가 오프라인일 때
        const userElement = document.getElementById(`user-${email}`);
        if (userElement) {
            userElement.remove(); // 오프라인 친구 목록에서 제거
            console.log(`User ${email} 유저 오프라인`);
        }
    }

    // 상태 변경 후 항상 온라인 친구 목록을 갱신
    fetchOnlineFriends(); // 서버에 현재 온라인 친구 목록 요청
}

// 읽음 상태 업데이트 처리 함수
function updateReadStatus(messageId, isRead) {
    console.log(`updateReadStatus 호출됨: messageId=${messageId}, isRead=${isRead}`);
    let messageElement = document.querySelector(`[data-message-id="${messageId}"]`);

    if (messageElement) {
        console.log(`messageElement 찾음:`, messageElement);
        let statusElement = messageElement.querySelector('.read-status');

        if (statusElement) {
            statusElement.textContent = isRead ? ' ' : ' 1 ';
            console.log(`읽음 상태 업데이트: messageId=${messageId}, isRead=${isRead}`);
        } else {
            console.warn(`'read-status' 클래스를 찾을 수 없음: messageId=${messageId}`);
        }
    } else {
        console.warn(`'data-message-id' 속성을 가진 요소를 찾을 수 없음: messageId=${messageId}`);
    }
}




// 알림 표시 함수
function displayNotification(senderEmail, content) {
   console.log("displayNotification 실행@@@@@@@");
       // 알림 표시 로직 구현 (예: 브라우저 알림, UI 팝업 등)
       alert(`새로운 메시지 도착 - ${senderEmail}, 내용: ${content}`);
   }


    socket.onclose = function(event) {
        console.log("WebSocket 연결 종료.", event);
    };

    socket.onerror = function(error) {
        console.error("WebSocket 오류:", error);
    };

    // 브라우저 종료 > 소켓 연결 종료
    window.onbeforeunload = function() {
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send(JSON.stringify({
                type: "statusUpdate",
                payload: {
                    email: userEmail,
                    isOnline: false
                }
            }));
            socket.close();
        }
    };

}