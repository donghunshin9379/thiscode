const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
console.log('토큰 : ' + csrfToken);  // + 연산자를 사용하여 문자열과 변수를 연결
console.log('토큰헤더 : ' + csrfHeader);  // + 연산자를 사용하여 문자열과 변수를 연결

document.getElementById('signupForm').addEventListener('submit', function(event) {
    event.preventDefault(); // 기본 제출 동작 방지

    // 입력값 가져오기
    const email = document.getElementById('email').value;
    const nickname = document.getElementById('nickname').value;
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const birthYear = document.getElementById('birthYear').value;
    const birthMonth = document.getElementById('birthMonth').value.padStart(2, '0'); // 월을 2자리로 보장
    const birthDay = document.getElementById('birthDay').value.padStart(2, '0'); // 일을 2자리로 보장

    const localDateString = `${birthYear}-${birthMonth}-${birthDay}`;

    fetch('/signup', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        },
        body: JSON.stringify({
            email: email,
            nickname: nickname,
            username: username,
            password: password,
            localDate: localDateString
        })
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(text => { throw new Error(text.error || text.message); });
        }
        return response.json();
    })
    .then(data => {
         // 성공 메시지 출력
        alert(data.message);
        // 회원가입 성공 후 페이지 이동
        window.location.href = '/login';
    })
    .catch(error => {
        console.error('Error:', error.message);
        alert(error.message);
    });
});

// 이메일 중복 검사
document.getElementById('email').addEventListener('input', function() {
    const email = this.value; // 입력한 이메일 값
    const messageElement = document.getElementById('email-duplicate-message');
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // 이메일 형식 정규 표현식

    // 이메일 유효성 검사
    if (email) {
        if (!emailPattern.test(email)) {
            messageElement.innerText = '유효한 이메일 형식이 아닙니다.'; // 이메일 형식 오류 메시지
            messageElement.style.display = 'block'; // 메시지 보이기
            return; // 유효하지 않은 경우 요청하지 않음
        }

        fetch(`/check-email?email=${encodeURIComponent(email)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        })
        .then(response => {
            if (response.ok) {
                return response.json(); // 응답을 JSON으로 변환
            } else {
                throw new Error('서버 오류');
            }
        })
        .then(data => {
            if (data.exists) {
                messageElement.innerText = '이미 존재하는 이메일입니다.'; // 중복 메시지 표시
                messageElement.style.display = 'block'; // 메시지 보이기
            } else {
                messageElement.innerText = ''; // 중복 메시지 제거
                messageElement.style.display = 'none'; // 메시지 숨기기
            }
        })
        .catch(error => {
            console.error('Error:', error.message);
            messageElement.innerText = '이메일 확인 중 오류가 발생했습니다.'; // 오류 메시지 표시
            messageElement.style.display = 'block'; // 메시지 보이기
        });
    } else {
        messageElement.innerText = ''; // 입력값이 없을 경우 메시지 제거
        messageElement.style.display = 'none'; // 메시지 숨기기
    }
});


