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
                   break;
               case "onlineFriends":
                   displayOnlineFriends(response.friends);
                   document.getElementById('online-section').style.display = 'block'; // 섹션 표시
                   break;
               default:
                   console.warn("알 수 없는 요청:", response.type);
           }
       } catch (error) {
           console.error("메세지 수신 요청 에러:", error);
       }
   };

   function displayOnlineFriends(onlineFriends) {
       const onlineUsersList = document.getElementById('onlineUsersList');
       onlineUsersList.innerHTML = '';

       onlineFriends.forEach(friend => {
           const userElement = document.createElement('li');
           userElement.id = `user-${friend}`;
           userElement.textContent = `${friend} is online`;
           onlineUsersList.appendChild(userElement);
       });
       console.log("displayOnlineFriends 함수 실행:", onlineFriends);
   }

  function updateUserStatus(email, status) {
      const onlineUsersList = document.getElementById('onlineUsersList');

      if (status === 'online') {
          if (!document.getElementById(`user-${email}`)) {
              const userElement = document.createElement('li');
              userElement.id = `user-${email}`;
              userElement.textContent = `${email} is online`;
              onlineUsersList.appendChild(userElement);
              console.log(`User ${email} 유저 온라인`);
          }
      } else {
          const userElement = document.getElementById(`user-${email}`);
          if (userElement) {
              userElement.remove();
              console.log(`User ${email} 유저 오프라인`);
          }
      }
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