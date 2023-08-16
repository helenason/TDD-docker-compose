package wanted.backend.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wanted.backend.Jwt.JwtUtil;
import wanted.backend.Domain.Member.*;
import wanted.backend.Domain.ResponseDto;
import wanted.backend.Repository.MemberRepository;
import wanted.backend.Repository.RefreshTokenRepository;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public ResponseDto joinMember(AuthDto authDto) {

        ResponseDto responseDto = new ResponseDto();

        String email = authDto.getEmail();
        String password = authDto.getPassword();

        if (!email.contains("@")) {
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
            responseDto.setMessage("이메일 형식을 맞춰주세요.");
            return responseDto;
        }
        if (memberRepository.existsByEmail(email)) {
            responseDto.setStatus(HttpStatus.CONFLICT);
            responseDto.setMessage("중복된 이메일입니다.");
            return responseDto;
        }
        if (password.length() < 8) {
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
            responseDto.setMessage("비밀번호 조건을 충족해주세요.");
            return responseDto;
        }

        String encodedPassword = passwordEncoder.encode(password);

        Member newMember = Member.builder()
                .email(email)
                .password(encodedPassword)
                .build();

        memberRepository.save(newMember);

        return responseDto;
    }

    @Transactional
    public ResponseDto loginJwt(AuthDto authDto, HttpServletResponse response) {

        ResponseDto responseDto = new ResponseDto();

        String email = authDto.getEmail();
        String password = authDto.getPassword();

        if (!email.contains("@")) {
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
            responseDto.setMessage("이메일 형식을 맞춰주세요.");
            return responseDto;
        }
        if (password.length() < 8) {
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
            responseDto.setMessage("비밀번호 조건을 충족해주세요.");
            return responseDto;
        }

        Member member = memberRepository.findByEmail(email).orElse(null);

        if (member == null || !passwordEncoder.matches(password, member.getPassword())) {
            responseDto.setStatus(HttpStatus.NOT_FOUND);
            responseDto.setMessage("올바른 이메일 혹은 비밀번호를 입력해주세요.");
            return responseDto;
        }

        TokenDto newTokenDto = jwtUtil.createAllToken(email);

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByMember(member);

        if (refreshToken.isPresent()) { // 존재

            refreshToken.get().updateToken(newTokenDto.getRefreshToken());

        } else { // 부재

            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .member(member)
                            .token(newTokenDto.getRefreshToken())
                            .build());
        }

        jwtUtil.setCookieAccessToken(response, newTokenDto.getAccessToken());
        jwtUtil.setCookieRefreshToken(response, newTokenDto.getRefreshToken());

        responseDto.setData(newTokenDto);

        return responseDto;
    }
}
