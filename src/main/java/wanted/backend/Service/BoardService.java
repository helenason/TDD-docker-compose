package wanted.backend.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import wanted.backend.Domain.Board.Board;
import wanted.backend.Domain.Board.BoardDto;
import wanted.backend.Domain.Member.Member;
import wanted.backend.Domain.ResponseDto;
import wanted.backend.Repository.BoardRepository;

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
            responseDto.setMessage("not enough data");
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
}
