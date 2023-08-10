package wanted.backend.Jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import wanted.backend.Domain.Member.RefreshToken;
import wanted.backend.Domain.Member.TokenDto;
import wanted.backend.Domain.ResponseDto;
import wanted.backend.Repository.RefreshTokenRepository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final Long expired_access = 1000 * 60 * 5L; // 5 min
    private static final Long expired_refresh = 1000 * 60 * 10L; // 10 min

    private final RefreshTokenRepository refreshTokenRepository;

    public String getEmailFromToken(String token) {

        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
                .getBody().get("email", String.class);
    }

    public Boolean tokenValid(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            log.error("tokenValid error = {}", ex.getMessage());
            return false;
        }
    }

    public Boolean refreshTokenValid(String token) {

        if (!tokenValid(token)) return false;

        String email = getEmailFromToken(token);

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByEmail(email);

        return refreshToken.isPresent() && token.equals(refreshToken.get().getToken());
    }

    public TokenDto createAllToken(String email) {
        return new TokenDto(createToken(email, "Access"), createToken(email, "Refresh"));
    }

    public String createToken(String email, String type) {

        Claims claims = Jwts.claims();
        claims.put("email", email);

        long time = type.equals("Access") ? expired_access : expired_refresh;

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String getCookieToken(HttpServletRequest request, String type) {

        String token = type.equals("Access") ? "Access_Token" : "Refresh_Token";

        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(token))
                .findFirst().map(Cookie::getValue)
                .orElse(null);
    }

    public void setCookieAccessToken(HttpServletResponse response, String accessToken) {

        Cookie cookie = new Cookie("Access_Token", accessToken);

        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        cookie.setMaxAge(60 * 5); // 5 min

        response.addCookie(cookie);
    }

    public void setCookieRefreshToken(HttpServletResponse response, String refreshToken) {

        Cookie cookie = new Cookie("Refresh_Token", refreshToken);

        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        cookie.setMaxAge(60 * 10); // 10 min

        response.addCookie(cookie);
    }

    public void invalidTokenResponse(HttpServletResponse response) throws IOException {

        removeTokenCookie(response);

        // message
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("utf-8");

        ResponseDto responseDto = new ResponseDto();
        responseDto.setStatus(HttpStatus.UNAUTHORIZED);
        responseDto.setMessage("expired token");

        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(responseDto);

        PrintWriter writer = response.getWriter();
        writer.print(result);
    }

    public void removeTokenCookie(HttpServletResponse response) {

        Cookie access_cookie = new Cookie("Access_Token", null);
        Cookie refresh_cookie = new Cookie("Refresh_Token", null);

        access_cookie.setPath("/");
        refresh_cookie.setPath("/");

        access_cookie.setMaxAge(0);
        refresh_cookie.setMaxAge(0);

        response.addCookie(access_cookie);
        response.addCookie(refresh_cookie);
    }
}