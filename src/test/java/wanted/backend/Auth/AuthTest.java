package wanted.backend.Auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import wanted.backend.Domain.Member.AuthDto;
import wanted.backend.Domain.Member.Member;
import wanted.backend.Domain.ResponseDto;
import wanted.backend.Repository.MemberRepository;
import wanted.backend.Service.AuthService;

@SpringBootTest
@DisplayName("Authorization Test")
public class AuthTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void join() {

        AuthDto authDto = new AuthDto("test@gmail.com", "test123!");

        authService.joinMember(authDto);

        Member findMember = memberRepository.findAll().get(0);
        Assertions.assertEquals(memberRepository.count(), 1);
        Assertions.assertEquals(findMember.getEmail(), authDto.getEmail());

    }

    @Test
    @DisplayName("회원가입 - 유효하지 않은 이메일")
    void joinInvalidEmail() {

        AuthDto authDto = new AuthDto("test", "test123!");

        ResponseDto responseDto = authService.joinMember(authDto);

        Assertions.assertEquals(responseDto.getMessage(), "invalid email");
        Assertions.assertEquals(memberRepository.count(), 0);

    }

    @Test
    @DisplayName("회원가입 - 유효하지 않은 비밀번호")
    void joinInvalidPassword() {

        AuthDto authDto = new AuthDto("test@gmail.com", "test");

        ResponseDto responseDto = authService.joinMember(authDto);

        Assertions.assertEquals(responseDto.getMessage(), "invalid password");
        Assertions.assertEquals(memberRepository.count(), 0);

    }

    @Test
    @DisplayName("회원가입 - 중복된 이메일")
    void joinDuplicatedEmail() {

        AuthDto authDto1 = new AuthDto("test@gmail.com", "test123!");
        AuthDto authDto2 = new AuthDto("test@gmail.com", "test123!");

        ResponseDto responseDto1 = authService.joinMember(authDto1);
        ResponseDto responseDto2 = authService.joinMember(authDto2);

        Assertions.assertEquals(responseDto1.getStatus(), HttpStatus.OK);
        Assertions.assertEquals(responseDto2.getStatus(), HttpStatus.CONFLICT);
        Assertions.assertEquals(memberRepository.count(), 1);

    }
}
