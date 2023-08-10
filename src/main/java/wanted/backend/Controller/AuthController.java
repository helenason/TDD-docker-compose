package wanted.backend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import wanted.backend.Domain.Member.AuthDto;
import wanted.backend.Domain.ResponseDto;
import wanted.backend.Service.AuthService;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/join")
    public ResponseEntity<ResponseDto> join(@RequestBody AuthDto authDto) {

        ResponseDto responseDto = authService.joinMember(authDto);

        return ResponseEntity.status(responseDto.getStatus())
                .body(responseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(@RequestBody AuthDto authDto, HttpServletResponse response) {

        ResponseDto responseDto = authService.loginJwt(authDto, response);

        return ResponseEntity.status(responseDto.getStatus())
                .body(responseDto);
    }
}
