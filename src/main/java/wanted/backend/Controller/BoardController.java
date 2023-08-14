package wanted.backend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import wanted.backend.Domain.Board.BoardDto;
import wanted.backend.Domain.Member.Member;
import wanted.backend.Domain.Member.MemberPrincipal;
import wanted.backend.Domain.ResponseDto;
import wanted.backend.Service.BoardService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    @PostMapping("")
    public ResponseEntity<ResponseDto> writePost(@RequestBody BoardDto.Request boardDto,
                                                 @AuthenticationPrincipal MemberPrincipal memberPrincipal) {

        Member member = memberPrincipal.getMember();
        ResponseDto responseDto = boardService.writePost(boardDto, member);

        return ResponseEntity.status(responseDto.getStatus())
                .body(responseDto);
    }

    @GetMapping("")
    public ResponseEntity<ResponseDto> listPosts(@RequestParam(required = false, defaultValue = "1") Integer page) {

        ResponseDto responseDto = boardService.listPosts(page);

        return ResponseEntity.status(responseDto.getStatus())
                .body(responseDto);
    }
}