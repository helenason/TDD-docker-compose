package wanted.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import wanted.backend.Domain.Member.AuthDto;
import wanted.backend.Domain.Member.Member;
import wanted.backend.Domain.Token.TokenDto;
import wanted.backend.Domain.ResponseDto;
import wanted.backend.Jwt.JwtUtil;
import wanted.backend.Repository.BoardRepository;
import wanted.backend.Repository.MemberRepository;
import wanted.backend.Repository.RefreshTokenRepository;
import wanted.backend.Service.AuthService;

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Authorization Test")
public class AuthTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext ctx;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();

        boardRepository.deleteAll();
        refreshTokenRepository.deleteAll();
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

        Assertions.assertEquals(responseDto.getMessage(), "이메일 형식을 맞춰주세요.");
        Assertions.assertEquals(memberRepository.count(), 0);
    }

    @Test
    @DisplayName("회원가입 - 유효하지 않은 비밀번호")
    void joinInvalidPassword() {

        AuthDto authDto = new AuthDto("test@gmail.com", "test");

        ResponseDto responseDto = authService.joinMember(authDto);

        Assertions.assertEquals(responseDto.getMessage(), "비밀번호 조건을 충족해주세요.");
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

    @Test
    @DisplayName("로그인 - 성공")
    void login() throws Exception {

        // given
        ObjectMapper objectMapper = new ObjectMapper();

        AuthDto authDto = new AuthDto("test@gmail.com", "test123!");
        authService.joinMember(authDto);

        // when
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDto)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("Access_Token"))
                    .andExpect(cookie().exists("Refresh_Token"))
                    .andReturn();

        // then
        String cookieAccessToken = Objects.requireNonNull(result.getResponse().getCookie("Access_Token")).getValue();
        String cookieRefreshToken = Objects.requireNonNull(result.getResponse().getCookie("Refresh_Token")).getValue();

        String content = result.getResponse().getContentAsString();
        ResponseDto responseDto = objectMapper.readValue(content, ResponseDto.class);

        TokenDto responseToken = objectMapper.convertValue(responseDto.getData(), TokenDto.class);

        Assertions.assertEquals(responseToken.getAccessToken(), cookieAccessToken);
        Assertions.assertEquals(responseToken.getRefreshToken(), cookieRefreshToken);
        Assertions.assertEquals(refreshTokenRepository.count(), 1);
        Assertions.assertEquals(jwtUtil.getEmailFromToken(cookieAccessToken), authDto.getEmail()); // Token 주인 식별
    }

    @Test
    @DisplayName("로그인 - 유효하지 않은 이메일")
    void loginInvalidEmail() throws Exception {

        // given
        ObjectMapper objectMapper = new ObjectMapper();

        AuthDto saveDto = new AuthDto("test@gmail.com", "test123!");
        AuthDto loginDto = new AuthDto("test", "test123!");
        authService.joinMember(saveDto);

        // when
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

        // then
        String content = result.getResponse().getContentAsString();
        String message = objectMapper.readValue(content, ResponseDto.class).getMessage();
        Assertions.assertEquals(message, "이메일 형식을 맞춰주세요.");
    }
    @Test
    @DisplayName("로그인 - 유효하지 않은 비밀번호")
    void loginInvalidPassword() throws Exception {

        // given
        ObjectMapper objectMapper = new ObjectMapper();

        AuthDto saveDto = new AuthDto("test@gmail.com", "test123!");
        AuthDto loginDto = new AuthDto("test@gmail.com", "test");
        authService.joinMember(saveDto);

        // when
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

        // then
        String content = result.getResponse().getContentAsString();
        String message = objectMapper.readValue(content, ResponseDto.class).getMessage();
        Assertions.assertEquals(message, "비밀번호 조건을 충족해주세요.");
    }

    @Test
    @DisplayName("로그인 - 실패")
    void loginFail() throws Exception {

        // given
        ObjectMapper objectMapper = new ObjectMapper();

        AuthDto saveDto = new AuthDto("test@gmail.com", "test123!");
        AuthDto loginDto = new AuthDto("test@gmail.com", "test123!@");
        authService.joinMember(saveDto);

        // when
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isNotFound())
                    .andReturn();

        // then
        String content = result.getResponse().getContentAsString();
        String message = objectMapper.readValue(content, ResponseDto.class).getMessage();
        Assertions.assertEquals(message, "올바른 이메일 혹은 비밀번호를 입력해주세요.");
    }
}
