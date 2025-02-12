# thiscode

## Discord를 *Java/Spring boot*로 구현합니다.

- 프레임워크 : 스프링부트 '3.3.3'
- 패키지 구조 : MVC
- DB ER 다이어그램
- ![Image](https://github.com/user-attachments/assets/62d7d467-6794-4000-99bf-c0eff50268b3)
- 의존성 [Spring Data JPA, jdbc, Junit, log4j, thymeleaf, Spring Security]

**현재 기능**
- 회원가입 / 로그인 (Spring Secutiry + Maria DB)
- 친구상태 변경 [요청, 대기, 차단] (Spring Data JPA + Maria DB)
- 친구목록 (Spring Data JPA + Maria DB)
- 온라인 친구 표시 (WebSocket)
- 실시간 메세지 / 읽음상태 (WebSocket)
- 메세지 알림 (WebSocket)
  
**목표 기능**
- 음성 통화 (WebRTC)

## 주요 로직 흐름

  **친구요청 [ 발신, 수신 ]**
  1. 친구요청 생성
  2. 유효성 검사 (유저 존재여부)
  3. DB에 요청 저장
  ![Image](https://github.com/user-attachments/assets/3c3ccfca-645d-4bc4-8442-303d75815d78)

  
  **온라인 상태**
  1. 유저 로그인 시 상태 업데이트
  2. 서버 측 사용자의 온라인 상태인 친구 필터링
  3. 온라인 상태 친구세션으로 상태 전송

  **실시간 메시지 전송**
  1. 유저가 서버에 채팅 송신 요청
  2. 유효성 검사 (유저 존재여부)
  3. DB에 메시지 저장
  4. 유저(발신자)에게 메시지 읽음상태 전송
  5. 친구 세션 유효성 검사
  6. 전송
  ![Image](https://github.com/user-attachments/assets/d652f5c1-e12e-4309-886d-1345c91828f9)

  **알림 전송**
  1. 채팅 송신 요청
  2. 친구유저 채팅방 입장 여부, 친구세션 존재여부 유효성 검사 
  3. 친구유저 온라인상태 O, 채팅방 입장 X (채팅방에 입장 후엔 알림 X)
  4. 알림 전송
  ![Image](https://github.com/user-attachments/assets/72bcf7c4-dada-46b8-b88a-d7265128c062)
