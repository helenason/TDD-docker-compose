package wanted.backend.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import wanted.backend.Domain.Member.*;
import wanted.backend.Domain.ResponseDto;
import wanted.backend.Repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
            responseDto.setMessage("duplicated password");
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
}
