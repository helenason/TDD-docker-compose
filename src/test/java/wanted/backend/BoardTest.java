package wanted.backend;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import wanted.backend.Domain.Board.Board;
import wanted.backend.Domain.Board.BoardDto;
import wanted.backend.Domain.Board.BoardListDto;
import wanted.backend.Domain.Member.Member;
import wanted.backend.Domain.ResponseDto;
import wanted.backend.Repository.BoardRepository;
import wanted.backend.Repository.MemberRepository;
import wanted.backend.Service.AuthService;
import wanted.backend.Service.BoardService;

import java.util.List;

@SpringBootTest
@DisplayName("Board Test")
public class BoardTest {

    Member loginMember;

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        boardRepository.deleteAll();
        memberRepository.deleteAll();

        loginMember = memberRepository.save(
                Member.builder()
                        .email("test@gmail.com")
                        .password("test123!")
                        .build());
    }

    @Test
    @DisplayName("게시판 작성 - 성공")
    void write() {

        BoardDto.Request boardDto = new BoardDto.Request("title", "content");

        boardService.writePost(boardDto, loginMember);

        Board findBoard = boardRepository.findAll().get(0);
        Assertions.assertEquals(boardRepository.count(), 1);
        Assertions.assertEquals(findBoard.getTitle(), "title");
        Assertions.assertEquals(findBoard.getWriter().getId(), loginMember.getId());
    }

    @Test
    @DisplayName("게시판 작성 - 데이터 불충분")
    void writeEmptyData() {

        BoardDto.Request boardDto = new BoardDto.Request(null, "content");

        ResponseDto responseDto = boardService.writePost(boardDto, loginMember);

        Assertions.assertEquals(boardRepository.count(), 0);
        Assertions.assertEquals(responseDto.getStatus(), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("게시판 목록 조회 - 성공")
    void list() {

        // given
        int total_size = 23;
        for (int i = 0; i < total_size; i ++) {
            BoardDto.Request boardDto = new BoardDto.Request("제목" + (i + 1), "내용" + (i + 1));
            boardService.writePost(boardDto, loginMember);
        }

        // when
        int page = 3; int size = 5;
        ResponseDto responseDto = boardService.listPosts(page);

        // then
        List<BoardListDto> data = (List<BoardListDto>) responseDto.getData();

        Assertions.assertEquals(responseDto.getStatus(), HttpStatus.OK);
        Assertions.assertEquals(data.size(), size);
        for (int i = 0; i < size; i++) {
            int root = total_size - size * (page - 1);
            Assertions.assertEquals(data.get(i).getTitle(), "제목" + (root - i));
        }
    }

    @Test
    @DisplayName("게시판 목록 조회 - 결과 없음")
    void listEmpty() {

        // given
        int total_size = 10;
        for (int i = 0; i < total_size; i ++) {
            BoardDto.Request boardDto = new BoardDto.Request("제목" + (i + 1), "내용" + (i + 1));
            boardService.writePost(boardDto, loginMember);
        }

        // when
        int page = 1000; int size = 5;
        ResponseDto responseDto = boardService.listPosts(page);

        // then
        List<BoardListDto> data = (List<BoardListDto>) responseDto.getData();

        Assertions.assertEquals(responseDto.getStatus(), HttpStatus.OK);
        Assertions.assertEquals(data.size(), 0);
    }

    @Test
    @DisplayName("게시판 상세 조회 - 성공")
    void show() {

        // given
        BoardDto.Request boardDto = new BoardDto.Request("제목", "내용");
        boardService.writePost(boardDto, loginMember);

        // when
        Long postId = boardRepository.findAll().get(0).getId();
        ResponseDto responseDto = boardService.showPost(postId);

        // then
        BoardDto.Response data = (BoardDto.Response) responseDto.getData();

        Assertions.assertEquals(responseDto.getStatus(), HttpStatus.OK);
        Assertions.assertEquals(data.getTitle(), boardDto.getTitle());
        Assertions.assertEquals(data.getContent(), boardDto.getContent());
    }

    @Test
    @DisplayName("게시판 상세 조회 - 없는 글")
    void showInvalid() {

        // given
        BoardDto.Request boardDto = new BoardDto.Request("제목", "내용");
        boardService.writePost(boardDto, loginMember);

        // when
        long postId = boardRepository.findAll().get(0).getId() + 12345L;
        ResponseDto responseDto = boardService.showPost(postId);

        // then
        Assertions.assertEquals(responseDto.getStatus(), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals(responseDto.getMessage(), "invalid post");
    }

    @Test
    @DisplayName("게시판 수정 - 성공")
    void update() {

        // given
        BoardDto.Request boardDto = new BoardDto.Request("제목", "내용");
        boardService.writePost(boardDto, loginMember);
        BoardDto.Request updateDto = new BoardDto.Request("수정 제목", "수정 내용");

        // when
        long postId = boardRepository.findAll().get(0).getId();
        ResponseDto responseDto = boardService.updatePost(postId, updateDto, loginMember);

        // then
        Assertions.assertEquals(responseDto.getStatus(), HttpStatus.CREATED);
        Assertions.assertEquals(boardRepository.findById(postId).get().getTitle(), "수정 제목");
        Assertions.assertEquals(boardRepository.findById(postId).get().getContent(), "수정 내용");
    }

    @Test
    @DisplayName("게시판 수정 - 작성자 아님")
    void updateNotWriter() {

        // given
        BoardDto.Request boardDto = new BoardDto.Request("제목", "내용");
        boardService.writePost(boardDto, loginMember);

        Member guestMember = memberRepository.save(
                Member.builder()
                        .email("guest@gmail.com")
                        .password("guest123!")
                        .build());

        BoardDto.Request updateDto = new BoardDto.Request("수정 제목", "수정 내용");

        // when
        long postId = boardRepository.findAll().get(0).getId();
        ResponseDto responseDto = boardService.updatePost(postId, updateDto, guestMember);

        // then
        Assertions.assertEquals(responseDto.getStatus(), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals(responseDto.getMessage(), "not writer");
    }
}