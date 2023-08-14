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
    }

    @Test
    @DisplayName("게시판 작성 - 성공")
    void write() {

        Member member = memberRepository.save(
                Member.builder()
                        .email("test@gmail.com")
                        .password("test123!")
                        .build());
        BoardDto.Request boardDto = new BoardDto.Request("title", "content");

        boardService.writePost(boardDto, member);

        Board findBoard = boardRepository.findAll().get(0);
        Assertions.assertEquals(boardRepository.count(), 1);
        Assertions.assertEquals(findBoard.getTitle(), "title");
        Assertions.assertEquals(findBoard.getWriter().getId(), member.getId());
    }

    @Test
    @DisplayName("게시판 작성 - 데이터 불충분")
    void writeEmptyData() {

        Member member = memberRepository.save(
                Member.builder()
                        .email("test@gmail.com")
                        .password("test123!")
                        .build());
        BoardDto.Request boardDto = new BoardDto.Request(null, "content");

        ResponseDto responseDto = boardService.writePost(boardDto, member);

        Assertions.assertEquals(boardRepository.count(), 0);
        Assertions.assertEquals(responseDto.getStatus(), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("게시판 목록 조회 - 성공")
    void list() {

        // given
        Member member = memberRepository.save(
                Member.builder()
                        .email("test@gmail.com")
                        .password("test123!")
                        .build());

        int total_size = 23;
        for (int i = 0; i < total_size; i ++) {
            BoardDto.Request boardDto = new BoardDto.Request("제목" + (i + 1), "내용" + (i + 1));
            boardService.writePost(boardDto, member);
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
        Member member = memberRepository.save(
                Member.builder()
                        .email("test@gmail.com")
                        .password("test123!")
                        .build());

        int total_size = 10;
        for (int i = 0; i < total_size; i ++) {
            BoardDto.Request boardDto = new BoardDto.Request("제목" + (i + 1), "내용" + (i + 1));
            boardService.writePost(boardDto, member);
        }

        // when
        int page = 1000; int size = 5;
        ResponseDto responseDto = boardService.listPosts(page);

        // then
        List<BoardListDto> data = (List<BoardListDto>) responseDto.getData();

        Assertions.assertEquals(responseDto.getStatus(), HttpStatus.OK);
        Assertions.assertEquals(data.size(), 0);
    }
}
