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
            responseDto.setMessage("invalid email");
            return responseDto;
        }
        if (memberRepository.existsByEmail(email)) {
            responseDto.setStatus(HttpStatus.CONFLICT);
            responseDto.setMessage("duplicated email"); // TODO not needed?
            return responseDto;
        }
        if (password.length() < 8) {
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
            responseDto.setMessage("invalid password");
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
            responseDto.setMessage("invalid email");
            return responseDto;
        }
        if (password.length() < 8) {
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
            responseDto.setMessage("invalid password");
            return responseDto;
        }

        Member selectedMember = memberRepository.findByEmail(email).orElse(null);

        if (selectedMember == null || !passwordEncoder.matches(password, selectedMember.getPassword())) {
            responseDto.setStatus(HttpStatus.NOT_FOUND);
            responseDto.setMessage("wrong email or password");
            return responseDto;
        }

        TokenDto newTokenDto = jwtUtil.createAllToken(email);

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByEmail(email);

        if (refreshToken.isPresent()) { // 존재

            refreshToken.get().updateToken(newTokenDto.getRefreshToken());

        } else { // 부재

            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .token(newTokenDto.getRefreshToken())
                            .email(email)
                            .build());
        }

        jwtUtil.setCookieAccessToken(response, newTokenDto.getAccessToken());
        jwtUtil.setCookieRefreshToken(response, newTokenDto.getRefreshToken());

        responseDto.setData(newTokenDto);

        return responseDto;
    }
}
