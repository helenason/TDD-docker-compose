# TDD and docker-compose practice with CRUD project
**TDD** 방식과 **docker-compose** 배포를 위한 간단한 CRUD 프로젝트입니다.
## 어플리케이션 실행 방법
1. 테스트 코드 외 파일 build
```
./gradlew build -x test
```
2. `Dockerfile` build
```
docker-compose build
```
3. `docker-compose.yml` 실행을 통해 서버 접속
```
docker-compose up
```
엔드포인트는 아래와 같이 호출
```
localhost:8080/{endpoint}
```
## DB 테이블 구조
- 구조
  ![erd.png](img/erd.png)

- 매핑 관계 부연 설명

  |  테이블1  |  테이블2  |   관계   |
  |:------:|:-----------:|:------:|
  | member | refresh_token |  일대일   |
  | member |     board     |  일대다   |
## 구현 방법
- TDD 방식
  - 단위 테스트 코드 선구현 후, 해당 테스트 기준으로 메인 코드 작성
  - 각 함수의 기능을 빠르게 파악 & 테스트 문서 작성에 용이
## API 명세
| Method |    API URL     |   Request    | Explanation |
|:------:|:--------------:|:------------:|:-----------:|
|  POST  |   /auth/join   | RequestBody  |    회원가입     |
|  POST  |  /auth/login   | RequestBody  |     로그인     |
|  POST  |     /board     | RequestBody  |    글 작성     |
|  GET   | /board?page={} | RequestParam |   글 목록 조회   |
|  GET   |  /board/{id}   | PathVariable |   글 상세 조회   |
| PATCH  |  /board/{id}   | PathVariable |    글 수정     |
| DELETE |  /board/{id}   | PathVariable |    글 삭제     |

### 계정 인증
#### `POST` /auth/join: 회원가입
- Request
    ```
    localhost:8080/auth/join
    ```
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
#### `POST` /auth/login: 로그인
- Request
    ```
    localhost:8080/auth/login
    ```
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
      ![cookie.png](img/cookie.png)
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
### 게시판
#### `POST` /board: 글 작성
- Request
    ```
    localhost:8080/board
    ```
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
#### `GET` /board: 글 목록 조회
  - Request
      ```
      localhost:8080/board?page=2
      ```
  - Response
    - 성공
      <br>(글 다섯 개씩 페이지네이션)
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
#### `GET` /board/{id}:  글 상세 조회
- Request
    ```
    localhost:8080/board/2
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
#### `PATCH` /board/{id}: 글 수정
  - Request
      ```
      localhost:8080/board/2
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
#### `DELETE` /board/{id}: 글 삭제
- Request
    ```
    localhost:8080/board/2
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
