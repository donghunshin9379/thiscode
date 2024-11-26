// 이 js 파일이 호출되는 시점에 즉시 WebSocket 객체가 생성되는것.

// WebSocket 연결 생성
const socket = new WebSocket(`ws://${window.location.host}/user-status`);
const statusDiv = document.getElementById('status');

// 연결이 열리면
socket.addEventListener('open', function (event) {
    console.log('서버와 웹소켓 연결 성공!');
    statusDiv.textContent = 'Connected to WebSocket';
    socket.send('Hello WebSocket Server!');
});

// 연결 종료 시
socket.addEventListener('close', function (event) {
    console.log('서버와 연결이 종료되었습니다.');
    statusDiv.textContent = 'Disconnected from WebSocket';
});

// 에러 발생 시
socket.addEventListener('error', function (event) {
    console.error('웹소켓 에러 발생:', event);
    statusDiv.textContent = 'WebSocket Error Occurred';
});