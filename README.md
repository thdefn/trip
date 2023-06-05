# 개요
네이버 밴드 같은 느낌의 여행 기록장
https://www.figma.com/file/L7MtdZ6st95sgtKKnsskzq/Untitled?type=design&node-id=0-1&t=3EJHM4JF15Vqd0yc-0

# ERD
https://www.erdcloud.com/d/v7bEz5NzwbPZzg7qK

Use : Spring, Jpa, Mysql, Redis, Docker, GCP, QueryDsl
Goal : QueryDsl을 활용해 쿼리 최적화에 신경써보기

### 회원가입
- [x] 아이디(필수값), 비밀번호(필수값), 전화번호(필수값), 인증번호(필수값)을 받아 회원가입한다
- [ ] 닉네임은 랜덤값으로 자동 생성되며 유저는 닉네임을 변경할 수 있다

### 유저
- [x] 기록장을 만드는 기능
  - [x] 이름(필수값)과 설명을 등록한다
  - [x] 이름과 설명을 수정할 수 있다
  - [x] 함께할 친구를 초대한다
- [x] 기록장 조회 기능
  - [x] 기록장 이름, 설명, 참여하는 멤버들의 프로필 이미지, 로케이션 목록을 확인할 수 있다
- [x] 여행을 기록하는 기능 (게시물)
  - [x] 사진(필수값)과 위치(필수값), 내용으로 여행을 기록한다
  - [x] 사진과 내용을 수정할 수 있다
- [x] 기록장 게시물 조회 기능
  - [x] 위치, 내용, 댓글 리스트, 이미지, 좋아요 수를 확인할 수 있다
- [x] 기록장 게시물 삭제 기능
  - [x] 특정 위치에 게시물이 하나 뿐인 경우 게시물을 삭제할 경우 해당 위치도 삭제된다
- [x] 위치별 기록장 이미지 조회 기능
  - [x] 위치를 리스트로 조회한다
  - [x] 위치, 등록된 이미지 리스트, 게시물을 등록한 멤버들의 프로필 이미지, 각 게시물의 댓글 수를 확인할 수 있다
- [x] 댓글 기능
  - [x] 게시물에 내용(필수값)으로 댓글을 단다
- [x] 좋아요 기능
  - [x] 댓글과 게시물에 좋아요를 누를 수 있다
- [x] 알림 기능
  - [x] 유저의 게시물에 댓글이 달리면 유저에게 알림이 간다
  - [x] 알림이 생성된 일시와 알림 내용을 확인할 수 있다
  - [x] 알림을 누르면 해당 게시물로 이동한다

### 채팅 기능 (추후 구현)
- [ ] 채팅과 가장 마지막의 위치 정보는 같이 저장된다
- [ ] 위치별 기록장 이미지 조회 기능의 위치를 눌러 해당 위치에서 채팅이 시작된 처음으로 이동할 수 있다
