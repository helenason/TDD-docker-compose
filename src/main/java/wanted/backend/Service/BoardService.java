package wanted.backend.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wanted.backend.Domain.Board.Board;
import wanted.backend.Domain.Board.BoardDto;
import wanted.backend.Domain.Board.BoardListDto;
import wanted.backend.Domain.Member.Member;
import wanted.backend.Domain.ResponseDto;
import wanted.backend.Repository.BoardRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    public ResponseDto writePost(BoardDto.Request boardDto, Member member) {

        ResponseDto responseDto = new ResponseDto();

        String title = boardDto.getTitle();
        String content = boardDto.getContent();

        if (title == null || content == null) {
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
            responseDto.setMessage("제목 혹은 내용을 입력해주세요.");
            return responseDto;
        }

        Board newBoard = Board.builder()
                .title(title)
                .content(content)
                .writer(member)
                .build();

        boardRepository.save(newBoard);

        return responseDto;
    }

    public ResponseDto listPosts(Integer page) {

        ResponseDto responseDto = new ResponseDto();
        List<BoardListDto> result = new ArrayList<>();

        Pageable pageable = PageRequest.of(
                page - 1, 5, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Board> postList = boardRepository.findAll(pageable).getContent();

        for (Board post : postList) {
             BoardListDto item = BoardListDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .date(post.getCreatedAt())
                    .writer(post.getWriter().getEmail())
                    .build();
            result.add(item);
        }
        responseDto.setData(result);

        return responseDto;
    }

    public ResponseDto showPost(long id) {

        ResponseDto responseDto = new ResponseDto();

        Optional<Board> postOp = boardRepository.findById(id);

        if (postOp.isEmpty()) {
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
            responseDto.setMessage("존재하지 않는 글입니다.");
            return responseDto;
        }

        Board post = postOp.get();

        responseDto.setData(BoardDto.Response.builder()
                .id(post.getId())
                .date(post.getCreatedAt())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getWriter().getEmail())
                .build());

        return responseDto;
    }

    @Transactional
    public ResponseDto updatePost(long id, BoardDto.Request boardDto, Member loginMember) {

        ResponseDto responseDto = new ResponseDto();

        Optional<Board> postOp = boardRepository.findById(id);

        if (postOp.isEmpty()) {
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
            responseDto.setMessage("존재하지 않는 글입니다.");
            return responseDto;
        }

        Board post = postOp.get();

        if (!post.getWriter().getId().equals(loginMember.getId())) {
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
            responseDto.setMessage("해당 글의 작성자가 아닙니다.");
            return responseDto;
        }

        post.updateBoard(boardDto.getTitle(), boardDto.getContent());

        responseDto.setStatus(HttpStatus.CREATED);

        return responseDto;
    }

    public ResponseDto deletePost(long id, Member loginMember) {

        ResponseDto responseDto = new ResponseDto();

        Optional<Board> postOp = boardRepository.findById(id);

        if (postOp.isEmpty()) {
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
            responseDto.setMessage("존재하지 않는 글입니다.");
            return responseDto;
        }

        Board post = postOp.get();

        if (!post.getWriter().getId().equals(loginMember.getId())) {
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
            responseDto.setMessage("해당 글의 작성자가 아닙니다.");
            return responseDto;
        }

        boardRepository.delete(post);

        responseDto.setStatus(HttpStatus.NO_CONTENT);

        return responseDto;
    }
}
