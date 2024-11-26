let socket;
let isOnlineTabActive = false; // 온라인 탭 활성화 여부
// 웹 소켓
window.addEventListener('load', connectWebSocket);

function connectWebSocket() {
    socket = new WebSocket("ws://localhost:8080/ws"); //WebSocketConfig에 CustomWebSocketHandler가 요청 처리

    socket.onopen = function(event) {
        console.log("WebSocket 연결 성공.");
//        const username = "사용자이름"; // 실제 사용자 이름으로 대체
//        socket.send(JSON.stringify({ type: 'login', username: username }));
    };

    socket.onclose = function(event) {
        console.log("WebSocket 연결 종료.", event);
    };

    socket.onerror = function(error) {
        console.error("WebSocket 오류:", error);
    };

    // 메시지를 수신했을 때 실행되는 이벤트 핸들러
    socket.onmessage = function(event) {
        const response = JSON.parse(event.data);

        if (response.type === 'onlineUsers') {
            displayOnlineUsers(response.users);
        }
    };
}



// 페이지가 언마운트될 때 연결 종료
window.onbeforeunload = function() {
    socket.close(1000, '유저가 나갔습니다');
};