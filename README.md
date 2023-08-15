# 지원자
손채영
# 실행 방법

# DB 테이블 구조

# API 동작 데모 영상 링크

# 구현 방법 및 이유

# API 명세
| Method |    API URL     |   Request    | Explanation |
|:------:|:--------------:|:------------:|:-----------:|
|  POST  |   /auth/join   | RequestBody  |    회원가입     |
|  POST  |  /auth/login   | RequestBody  |     로그인     |
|  POST  |     /board     | RequestBody  |    글 작성     |
|  GET   | /board?page={} | RequestParam |   글 목록 조회   |
|  GET   |  /board/{id}   | PathVariable |   글 상세 조회   |
| PATCH  |  /board/{id}   | PathVariable |    글 수정     |
| DELETE |  /board/{id}   | PathVariable |    글 삭제     |

## 계정 인증
### 회원가입 : POST /auth/join
- Request
    ```
    {
        "email" : "test@naver.com",
        "password" : "test123!"
    }
    ```
- Response
  - 성공
      ```
      200 OK
      {
          "status": "OK",
          "message": "성공적으로 완료되었습니다.",
          "data": null
      }
      ```
  - 실패
      ```
      400 BAD_REQUEST
      {
          "status": "BAD_REQUEST",
          "message": "이메일 형식을 맞춰주세요.",
          "data": null
      }
      ```
      ```
      400 BAD_REQUEST
      {
          "status": "BAD_REQUEST",
          "message": "비밀번호 조건을 충족해주세요.",
          "data": null
      }
      ```
      ```
      409 CONFLICT
      {
          "status": "CONFLICT",
          "message": "중복된 이메일입니다.",
          "data": null
      }
      ```
### 로그인 : POST /auth/login
- Request
    ```
    {
        "email" : "test@naver.com",
        "password" : "test123!"
    }
    ```
- Response
  - 성공
      ```
      200 OK
      {
          "status": "OK",
          "message": "성공적으로 완료되었습니다.",
          "data": {
              "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InRlc3RAbmF2ZXIuY29tIiwiaWF0IjoxNjkyMDk4MzU2LCJleHAiOjE2OTIxMDE5NTZ9.nC9HQu_uYE1VMSsrgDYMa7UCsznX6WWi9Uv7hWmiOQI",
              "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InRlc3RAbmF2ZXIuY29tIiwiaWF0IjoxNjkyMDk4MzU2LCJleHAiOjE2OTMzOTQzNTZ9.1QLdeKyrUSUHStBJR_2Abs5L704eLpV8ADDJqzT_ZLs"
          }
      }
      ```
    ![img.png](img.png)
  - 실패
    ```
    400 BAD_REQUEST
    {
        "status": "BAD_REQUEST",
        "message": "이메일 형식을 맞춰주세요.",
        "data": null
    }
    ```
    ```
    400 BAD_REQUEST
    {
        "status": "BAD_REQUEST",
        "message": "비밀번호 조건을 충족해주세요.",
        "data": null
    }
    ```
    ```
    404 NOT_FOUND
    {
        "status": "NOT_FOUND",
        "message": "올바른 이메일 혹은 비밀번호를 입력해주세요.",
        "data": null
    }
    ```
## 게시판
### 글 작성 : POST /board
- Request
    ```
    {
        "title" : "title123",
        "content" : "content123"
    }
    ```
- Response
  - 성공
    ```
    200 OK
    {
        "status": "OK",
        "message": "성공적으로 완료되었습니다.",
        "data": null
    }
    ```
  - 실패
    ```
    400 BAD_REQUEST
    {
        "status": "BAD_REQUEST",
        "message": "제목 혹은 내용을 입력해주세요.",
        "data": null
    }
    ```
    ```
    401 UNAUTHORIZED
    {
        "status": "UNAUTHORIZED",
        "message": "로그인이 필요합니다.",
        "data": null
    }
    ```
### 글 목록 조회 : GET /board
  - Request
      ```
      /board?page=2
      ```
    - Response
      - 성공
      ```
      200 OK
      {
          "status": "OK",
          "message": "성공적으로 완료되었습니다.",
          "data": [
              {
                  "id": 2,
                  "title": "title2",
                  "date": "2023-08-15T20:24:46.313729",
                  "writer": "test@naver.com"
              },
              {
                  "id": 1,
                  "title": "title1",
                  "date": "2023-08-15T20:23:32.527841",
                  "writer": "test@naver.com"
              }
          ]
      }
      ```
      ```
      200 OK
      {
          "status": "OK",
          "message": "성공적으로 완료되었습니다.",
          "data": []
      }
      ```
### 글 상세 조회 : GET /board/{id}
- Request
    ```
    /board/2
    ```
  - Response
    - 성공
    ```
    200 OK
    {
        "status": "OK",
        "message": "성공적으로 완료되었습니다.",
        "data": {
            "id": 2,
            "title": "title",
            "content": "content",
            "date": "2023-08-15T20:41:26.909512",
            "writer": "test@naver.com"
        }
    }
    ```
    - 실패
    ```
    400 BAD_REQUEST
    {
        "status": "BAD_REQUEST",
        "message": "존재하지 않는 글입니다.",
        "data": null
    }
    ```
### 글 수정 : PATCH /board/{id}
  - Request
      ```
      /board/2
      ```
    - Response
      - 성공
      ```
      200 OK
      {
          "status": "OK",
          "message": "성공적으로 완료되었습니다.",
          "data": null
      }
      ```
      - 실패
      ```
      400 BAD_REQUEST
      {
          "status": "BAD_REQUEST",
          "message": "존재하지 않는 글입니다.",
          "data": null
      }
      ```
      ```
      400 BAD_REQUEST
      {
          "status": "BAD_REQUEST",
          "message": "해당 글의 작성자가 아닙니다.",
          "data": null
      }
      ```
      ```
      401 UNAUTHORIZED
      {
          "status": "UNAUTHORIZED",
          "message": "로그인이 필요합니다.",
          "data": null
      }
      ```
### 글 삭제 : DELETE /board/{id}
- Request
    ```
    /board/2
    ```
  - Response
    - 성공
    ```
    200 OK
    {
        "status": "OK",
        "message": "성공적으로 완료되었습니다.",
        "data": null
    }
    ```
    - 실패
    ```
    400 BAD_REQUEST
    {
        "status": "BAD_REQUEST",
        "message": "존재하지 않는 글입니다.",
        "data": null
    }
    ```
    ```
    400 BAD_REQUEST
    {
        "status": "BAD_REQUEST",
        "message": "해당 글의 작성자가 아닙니다.",
        "data": null
    }
    ```
    ```
    401 UNAUTHORIZED
    {
        "status": "UNAUTHORIZED",
        "message": "로그인이 필요합니다.",
        "data": null
    }
    ```